package io.github.lucaargolo.latte.screen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.TickableElement;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.apache.commons.lang3.ClassUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@SuppressWarnings("unused")
public class ConfigurableWidget<V> extends ElementListWidget.Entry<ConfigurableWidget<?>> implements TickableElement {

    private final TextRenderer textRenderer;
    private final Text text;

    private final int offset;
    private String key;
    private final V value;
    private final Function<String, V> valueValidator;

    private final Class<V> arrayReference;
    private final Map.Entry<Class<?>, Class<V>> mapReference;

    private final TextFieldWidget textField;
    private final ButtonWidget addEntryButton;
    private final ButtonWidget removeEntryButton;
    private final ButtonWidget resetButton;

    private final List<Element> children = new ArrayList<>();

    private boolean isRemoved = false;
    private boolean isAdding = false;

    private ConfigurableWidget(TextRenderer textRenderer, Text text, int offset, int width, int height, @NotNull String key, @Nullable V value, @Nullable Function<String, V> valueValidator, @Nullable Class<V> arrayReference, @Nullable Map.Entry<Class<?>, Class<V>> mapReference) {
        this.textRenderer = textRenderer;
        this.text = text;
        this.offset = offset;

        this.key = key;
        this.value = value;
        this.valueValidator = valueValidator;

        this.arrayReference = arrayReference;
        this.mapReference = mapReference;

        this.textField = new TextFieldWidget(textRenderer, width/2, 0, width/2-51, height, text);
        this.addEntryButton = new ButtonWidget(width-40, 0, 40, height+2, new TranslatableText("screen.latte.add"), (button) -> isAdding = true);
        this.removeEntryButton = new ButtonWidget(width-40, 0, 40, height+2, new TranslatableText("screen.latte.remove"), (button) -> isRemoved = true);
        this.resetButton = new ButtonWidget(width-40, 0, 40, height+2, new TranslatableText("controls.reset"), (button) -> textField.setText(""+value));

        this.children.add(textField);
        this.children.add(addEntryButton);
        this.children.add(removeEntryButton);
        this.children.add(resetButton);

        if(value != null) {
            this.textField.setText(value.toString());
        }
    }

    public void init(int width, int height) {
        if(arrayReference != null || mapReference != null) {
            if(value != null) {
                this.textField.visible = true;
                this.textField.x = width/2;
                this.textField.setWidth(width/2-101);

                this.addEntryButton.visible = false;

                this.removeEntryButton.visible = true;
                this.removeEntryButton.x = width-90;
                this.removeEntryButton.setWidth(40);

                this.resetButton.visible = true;
                this.resetButton.x = width-40;
                this.resetButton.setWidth(40);
            }else{
                if(valueValidator != null) {
                    this.textField.visible = false;

                    this.addEntryButton.visible = true;
                    try{
                        if(arrayReference != null) {
                            if(!ClassUtils.isPrimitiveOrWrapper(arrayReference)) {
                                arrayReference.newInstance();
                            }
                        }else{
                            if(!ClassUtils.isPrimitiveOrWrapper(mapReference.getKey())) {
                                mapReference.getKey().newInstance();
                            }
                            if(!ClassUtils.isPrimitiveOrWrapper(mapReference.getValue())) {
                                mapReference.getValue().newInstance();
                            }
                        }
                    } catch (Exception e) {
                        this.addEntryButton.active = false;
                    }

                    this.addEntryButton.x = width-90;
                    this.addEntryButton.setWidth(40);

                    this.removeEntryButton.visible = false;

                    this.resetButton.visible = true;
                    this.resetButton.x = width-40;
                    this.resetButton.setWidth(40);
                }else{
                    this.textField.visible = false;

                    this.addEntryButton.visible = false;

                    this.removeEntryButton.visible = true;
                    this.removeEntryButton.x = width-40;
                    this.removeEntryButton.setWidth(40);

                    this.resetButton.visible = false;
                }
            }
        }else if(value != null){
            this.textField.visible = true;
            this.textField.x = width/2;
            this.textField.setWidth(width/2-51);

            this.addEntryButton.visible = false;
            this.removeEntryButton.visible = false;

            this.resetButton.visible = true;
            this.resetButton.x = width-40;
            this.resetButton.setWidth(40);
        }else{
            this.textField.visible = false;
            this.addEntryButton.visible = false;
            this.removeEntryButton.visible = false;
            this.resetButton.visible = false;
        }
        this.textField.setText(textField.getText());
        this.resetButton.active = isResettable();
    }

    public boolean isRemoved() {
        return isRemoved;
    }

    public boolean isAdding() {
        return isAdding;
    }

    public void setAdding(boolean adding) {
        isAdding = adding;
    }

    public int getOffset() {
        return offset;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getOriginalValue() {
        if(value != null) {
            return value.toString();
        }
        return null;
    }

    public String getCurrentValue() {
        return textField.getText();
    }

    public Class<?> getKeyClass() {
        if(mapReference != null) {
            return mapReference.getKey();
        }else {
            return null;
        }
    }

    public Class<?> getValueClass() {
        if(arrayReference != null) {
            return arrayReference;
        }else if(mapReference != null) {
            return mapReference.getValue();
        }else{
            return null;
        }
    }

    public boolean isSavable() {
        if(value != null && valueValidator != null) {
            return valueValidator.apply(textField.getText()) != null;
        }
        return true;
    }

    public void save(JsonObject jsonObject) {
        if(value == null) {
            if(valueValidator != null && arrayReference != null) {
                jsonObject.add(key, new JsonArray());
            }else{
                jsonObject.add(key, new JsonObject());
            }
        }else{
            V writtenValue = valueValidator.apply(textField.getText());
            if(value instanceof Number) {
                if(writtenValue != null) {
                    jsonObject.addProperty(key, (Number) writtenValue);
                }else{
                    jsonObject.addProperty(key, 0);
                }
            }else if(value instanceof Boolean) {
                if(writtenValue != null) {
                    jsonObject.addProperty(key, (Boolean) writtenValue);
                }else{
                    jsonObject.addProperty(key, false);
                }
            }else if(value instanceof Character) {
                if(writtenValue != null) {
                    jsonObject.addProperty(key, (Character) writtenValue);
                }else{
                    jsonObject.addProperty(key, 'a');
                }
            }else {
                if(writtenValue != null) {
                    jsonObject.addProperty(key, writtenValue.toString());
                }else{
                    jsonObject.addProperty(key, "");
                }
            }
        }
    }

    public boolean isResettable() {
        if(resetButton.visible && valueValidator != null) {
            if(value != null) {
                if(valueValidator.apply(textField.getText()) != null) {
                    textField.setEditableColor(14737632);
                    return !value.toString().equals(textField.getText());
                }else{
                    textField.setEditableColor(0xFFFF0000);
                    return true;
                }
            }else if(arrayReference != null || mapReference != null) {
                //TODO: Fucking this
                return false;
            }
        }
        return false;
    }

    public void reset() {
        resetButton.onPress();
    }

    @Override
    public void tick() {
        textField.tick();
        resetButton.active = isResettable();
    }

    @Override
    public List<? extends Element> children() {
        return this.children;
    }

    @Override
    public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        textRenderer.draw(matrices, text, x+(offset *10), y+8, 0xFFFFFF);
        children.forEach(element -> {
            if(element instanceof AbstractButtonWidget) {
                ((AbstractButtonWidget) element).y = y-1;
                ((AbstractButtonWidget) element).render(matrices, mouseX, mouseY, tickDelta);
            }
            if(element instanceof TextFieldWidget) {
                ((TextFieldWidget) element).y++;
            }
        });
    }

    private static ConfigurableWidget<Integer> fromInt(TextRenderer textRenderer, Text text, int offset, int width, int height, String key, Integer value) {
        return new ConfigurableWidget<>(textRenderer, text, offset, width, height, key, value, s -> {
            try { return Integer.parseInt(s); }catch (Exception ignored) { return null; }
        }, null, null);
    }

    private static ConfigurableWidget<Integer> fromIntArray(TextRenderer textRenderer, Text text, int offset, int width, int height, String key, Integer value) {
        return new ConfigurableWidget<>(textRenderer, text, offset, width, height, key, value, s -> {
            try { return Integer.parseInt(s); }catch (Exception ignored) { return null; }
        }, Integer.class, null);
    }

    private static ConfigurableWidget<Integer> fromIntMap(TextRenderer textRenderer, Text text, int offset, int width, int height, String key, Integer value, Class<?> keyClass) {
        return new ConfigurableWidget<>(textRenderer, text, offset, width, height, key, value, s -> {
            try { return Integer.parseInt(s); }catch (Exception ignored) { return null; }
        }, null, new AbstractMap.SimpleEntry<>(keyClass, Integer.class));
    }

    private static ConfigurableWidget<Short> fromShort(TextRenderer textRenderer, Text text, int offset, int width, int height, String key, Short value) {
        return new ConfigurableWidget<>(textRenderer, text, offset, width, height, key, value, s -> {
            try { return Short.parseShort(s); }catch (Exception ignored) { return null; }
        }, null, null);
    }

    private static ConfigurableWidget<Short> fromShortArray(TextRenderer textRenderer, Text text, int offset, int width, int height, String key, Short value) {
        return new ConfigurableWidget<>(textRenderer, text, offset, width, height, key, value, s -> {
            try { return Short.parseShort(s); }catch (Exception ignored) { return null; }
        }, Short.class, null);
    }

    private static ConfigurableWidget<Short> fromShortMap(TextRenderer textRenderer, Text text, int offset, int width, int height, String key, Short value, Class<?> keyClass) {
        return new ConfigurableWidget<>(textRenderer, text, offset, width, height, key, value, s -> {
            try { return Short.parseShort(s); }catch (Exception ignored) { return null; }
        }, null, new AbstractMap.SimpleEntry<>(keyClass, Short.class));
    }

    private static ConfigurableWidget<Long> fromLong(TextRenderer textRenderer, Text text, int offset, int width, int height, String key, Long value) {
        return new ConfigurableWidget<>(textRenderer, text, offset, width, height, key, value, s -> {
            try { return Long.parseLong(s); }catch (Exception ignored) { return null; }
        }, null, null);
    }

    private static ConfigurableWidget<Long> fromLongArray(TextRenderer textRenderer, Text text, int offset, int width, int height, String key, Long value) {
        return new ConfigurableWidget<>(textRenderer, text, offset, width, height, key, value, s -> {
            try { return Long.parseLong(s); }catch (Exception ignored) { return null; }
        }, Long.class, null);
    }

    private static ConfigurableWidget<Long> fromLongMap(TextRenderer textRenderer, Text text, int offset, int width, int height, String key, Long value, Class<?> keyClass) {
        return new ConfigurableWidget<>(textRenderer, text, offset, width, height, key, value, s -> {
            try { return Long.parseLong(s); }catch (Exception ignored) { return null; }
        }, null, new AbstractMap.SimpleEntry<>(keyClass, Long.class));
    }

    private static ConfigurableWidget<Double> fromDouble(TextRenderer textRenderer, Text text, int offset, int width, int height, String key, Double value) {
        return new ConfigurableWidget<>(textRenderer, text, offset, width, height, key, value, s -> {
            try { return Double.parseDouble(s); }catch (Exception ignored) { return null; }
        }, null, null);
    }

    private static ConfigurableWidget<Double> fromDoubleArray(TextRenderer textRenderer, Text text, int offset, int width, int height, String key, Double value) {
        return new ConfigurableWidget<>(textRenderer, text, offset, width, height, key, value, s -> {
            try { return Double.parseDouble(s); }catch (Exception ignored) { return null; }
        }, Double.class, null);
    }

    private static ConfigurableWidget<Double> fromDoubleMap(TextRenderer textRenderer, Text text, int offset, int width, int height, String key, Double value, Class<?> keyClass) {
        return new ConfigurableWidget<>(textRenderer, text, offset, width, height, key, value, s -> {
            try { return Double.parseDouble(s); }catch (Exception ignored) { return null; }
        }, null, new AbstractMap.SimpleEntry<>(keyClass, Double.class));
    }

    private static ConfigurableWidget<Float> fromFloat(TextRenderer textRenderer, Text text, int offset, int width, int height, String key, Float value) {
        return new ConfigurableWidget<>(textRenderer, text, offset, width, height, key, value, s -> {
            try { return Float.parseFloat(s); }catch (Exception ignored) { return null; }
        }, null, null);
    }

    private static ConfigurableWidget<Float> fromFloatArray(TextRenderer textRenderer, Text text, int offset, int width, int height, String key, Float value) {
        return new ConfigurableWidget<>(textRenderer, text, offset, width, height, key, value, s -> {
            try { return Float.parseFloat(s); }catch (Exception ignored) { return null; }
        }, Float.class, null);
    }

    private static ConfigurableWidget<Float> fromFloatMap(TextRenderer textRenderer, Text text, int offset, int width, int height, String key, Float value, Class<?> keyClass) {
        return new ConfigurableWidget<>(textRenderer, text, offset, width, height, key, value, s -> {
            try { return Float.parseFloat(s); }catch (Exception ignored) { return null; }
        }, null, new AbstractMap.SimpleEntry<>(keyClass, Float.class));
    }

    private static ConfigurableWidget<Byte> fromByte(TextRenderer textRenderer, Text text, int offset, int width, int height, String key, Byte value) {
        return new ConfigurableWidget<>(textRenderer, text, offset, width, height, key, value, s -> {
            try { return Byte.parseByte(s); }catch (Exception ignored) { return null; }
        }, null, null);
    }

    private static ConfigurableWidget<Byte> fromByteArray(TextRenderer textRenderer, Text text, int offset, int width, int height, String key, Byte value) {
        return new ConfigurableWidget<>(textRenderer, text, offset, width, height, key, value, s -> {
            try { return Byte.parseByte(s); }catch (Exception ignored) { return null; }
        }, Byte.class, null);
    }

    private static ConfigurableWidget<Byte> fromByteMap(TextRenderer textRenderer, Text text, int offset, int width, int height, String key, Byte value, Class<?> keyClass) {
        return new ConfigurableWidget<>(textRenderer, text, offset, width, height, key, value, s -> {
            try { return Byte.parseByte(s); }catch (Exception ignored) { return null; }
        }, null, new AbstractMap.SimpleEntry<>(keyClass, Byte.class));
    }

    private static ConfigurableWidget<Boolean> fromBoolean(TextRenderer textRenderer, Text text, int offset, int width, int height, String key, Boolean value) {
        return new ConfigurableWidget<>(textRenderer, text, offset, width, height, key, value, s -> {
            if(s.equals("true")) { return Boolean.TRUE; }else if(s.equals("false")) { return Boolean.FALSE; } else return null;
        }, null, null);
    }

    private static ConfigurableWidget<Boolean> fromBooleanArray(TextRenderer textRenderer, Text text, int offset, int width, int height, String key, Boolean value) {
        return new ConfigurableWidget<>(textRenderer, text, offset, width, height, key, value, s -> {
            if(s.equals("true")) { return Boolean.TRUE; }else if(s.equals("false")) { return Boolean.FALSE; } else return null;
        }, Boolean.class, null);
    }

    private static ConfigurableWidget<Boolean> fromBooleanMap(TextRenderer textRenderer, Text text, int offset, int width, int height, String key, Boolean value, Class<?> keyClass) {
        return new ConfigurableWidget<>(textRenderer, text, offset, width, height, key, value, s -> {
            if(s.equals("true")) { return Boolean.TRUE; }else if(s.equals("false")) { return Boolean.FALSE; } else return null;
        }, null, new AbstractMap.SimpleEntry<>(keyClass, Boolean.class));
    }

    private static ConfigurableWidget<Character> fromChar(TextRenderer textRenderer, Text text, int offset, int width, int height, String key, Character value) {
        return new ConfigurableWidget<>(textRenderer, text, offset, width, height, key, value, s -> {
            if(s.toCharArray().length == 1) { return s.toCharArray()[0]; } else return null;
        }, null, null);
    }

    private static ConfigurableWidget<Character> fromCharArray(TextRenderer textRenderer, Text text, int offset, int width, int height, String key, Character value) {
        return new ConfigurableWidget<>(textRenderer, text, offset, width, height, key, value, s -> {
            if(s.toCharArray().length == 1) { return s.toCharArray()[0]; } else return null;
        }, Character.class, null);
    }

    private static ConfigurableWidget<Character> fromCharMap(TextRenderer textRenderer, Text text, int offset, int width, int height, String key, Character value, Class<?> keyClass) {
        return new ConfigurableWidget<>(textRenderer, text, offset, width, height, key, value, s -> {
            if(s.toCharArray().length == 1) { return s.toCharArray()[0]; } else return null;
        }, null, new AbstractMap.SimpleEntry<>(keyClass, Character.class));
    }

    private static ConfigurableWidget<String> fromString(TextRenderer textRenderer, Text text, int offset, int width, int height, String key, String value) {
        return new ConfigurableWidget<>(textRenderer, text, offset, width, height, key, value, s -> s, null, null);
    }

    private static ConfigurableWidget<String> fromStringArray(TextRenderer textRenderer, Text text, int offset, int width, int height, String key, String value) {
        return new ConfigurableWidget<>(textRenderer, text, offset, width, height, key, value, s -> s, String.class, null);
    }

    private static ConfigurableWidget<String> fromStringMap(TextRenderer textRenderer, Text text, int offset, int width, int height, String key, String value, Class<?> keyClass) {
        return new ConfigurableWidget<>(textRenderer, text, offset, width, height, key, value, s -> s, null, new AbstractMap.SimpleEntry<>(keyClass, String.class));
    }
    
    public static ConfigurableWidget<?> fromPrimitive(TextRenderer textRenderer, Text text, int offset, int width, int height, String key, JsonPrimitive primitive, Class<?> primitiveClass) {
        if(primitiveClass == int.class || primitiveClass == Integer.class) {
            return ConfigurableWidget.fromInt(textRenderer, text, offset, width, height, key, primitive.getAsInt());
        }else if(primitiveClass == short.class || primitiveClass == Short.class) {
            return ConfigurableWidget.fromShort(textRenderer, text, offset, width, height, key, primitive.getAsShort());
        }else if(primitiveClass == long.class || primitiveClass == Long.class) {
            return ConfigurableWidget.fromLong(textRenderer, text, offset, width, height, key, primitive.getAsLong());
        }else if(primitiveClass == double.class || primitiveClass == Double.class) {
            return ConfigurableWidget.fromDouble(textRenderer, text, offset, width, height, key, primitive.getAsDouble());
        }else if(primitiveClass == float.class || primitiveClass == Float.class) {
            return ConfigurableWidget.fromFloat(textRenderer, text, offset, width, height, key, primitive.getAsFloat());
        }else if(primitiveClass == byte.class || primitiveClass == Byte.class) {
            return ConfigurableWidget.fromByte(textRenderer, text, offset, width, height, key, primitive.getAsByte());
        }else if(primitiveClass == boolean.class || primitiveClass == Boolean.class) {
            return ConfigurableWidget.fromBoolean(textRenderer, text, offset, width, height, key, primitive.getAsBoolean());
        }else if(primitiveClass == char.class || primitiveClass == Character.class) {
            return ConfigurableWidget.fromChar(textRenderer, text, offset, width, height, key, primitive.getAsCharacter());
        }
        return ConfigurableWidget.fromString(textRenderer, text, offset, width, height, key, primitive.getAsString());
    }

    public static ConfigurableWidget<?> fromPrimitiveArray(TextRenderer textRenderer, Text text, int offset, int width, int height, String key, JsonPrimitive primitive, Class<?> primitiveClass) {
        if(primitiveClass == int.class || primitiveClass == Integer.class) {
            return ConfigurableWidget.fromIntArray(textRenderer, text, offset, width, height, key, primitive.getAsInt());
        }else if(primitiveClass == short.class || primitiveClass == Short.class) {
            return ConfigurableWidget.fromShortArray(textRenderer, text, offset, width, height, key, primitive.getAsShort());
        }else if(primitiveClass == long.class || primitiveClass == Long.class) {
            return ConfigurableWidget.fromLongArray(textRenderer, text, offset, width, height, key, primitive.getAsLong());
        }else if(primitiveClass == double.class || primitiveClass == Double.class) {
            return ConfigurableWidget.fromDoubleArray(textRenderer, text, offset, width, height, key, primitive.getAsDouble());
        }else if(primitiveClass == float.class || primitiveClass == Float.class) {
            return ConfigurableWidget.fromFloatArray(textRenderer, text, offset, width, height, key, primitive.getAsFloat());
        }else if(primitiveClass == byte.class || primitiveClass == Byte.class) {
            return ConfigurableWidget.fromByteArray(textRenderer, text, offset, width, height, key, primitive.getAsByte());
        }else if(primitiveClass == boolean.class || primitiveClass == Boolean.class) {
            return ConfigurableWidget.fromBooleanArray(textRenderer, text, offset, width, height, key, primitive.getAsBoolean());
        }else if(primitiveClass == char.class || primitiveClass == Character.class) {
            return ConfigurableWidget.fromCharArray(textRenderer, text, offset, width, height, key, primitive.getAsCharacter());
        }
        return ConfigurableWidget.fromStringArray(textRenderer, text, offset, width, height, key, primitive.getAsString());
    }

    public static ConfigurableWidget<?> fromPrimitiveMap(TextRenderer textRenderer, Text text, int offset, int width, int height, String key, Class<?> keyClass, JsonPrimitive primitive, Class<?> primitiveClass) {
        if(primitiveClass == int.class || primitiveClass == Integer.class) {
            return ConfigurableWidget.fromIntMap(textRenderer, text, offset, width, height, key, primitive.getAsInt(), keyClass);
        }else if(primitiveClass == short.class || primitiveClass == Short.class) {
            return ConfigurableWidget.fromShortMap(textRenderer, text, offset, width, height, key, primitive.getAsShort(), keyClass);
        }else if(primitiveClass == long.class || primitiveClass == Long.class) {
            return ConfigurableWidget.fromLongMap(textRenderer, text, offset, width, height, key, primitive.getAsLong(), keyClass);
        }else if(primitiveClass == double.class || primitiveClass == Double.class) {
            return ConfigurableWidget.fromDoubleMap(textRenderer, text, offset, width, height, key, primitive.getAsDouble(), keyClass);
        }else if(primitiveClass == float.class || primitiveClass == Float.class) {
            return ConfigurableWidget.fromFloatMap(textRenderer, text, offset, width, height, key, primitive.getAsFloat(), keyClass);
        }else if(primitiveClass == byte.class || primitiveClass == Byte.class) {
            return ConfigurableWidget.fromByteMap(textRenderer, text, offset, width, height, key, primitive.getAsByte(), keyClass);
        }else if(primitiveClass == boolean.class || primitiveClass == Boolean.class) {
            return ConfigurableWidget.fromBooleanMap(textRenderer, text, offset, width, height, key, primitive.getAsBoolean(), keyClass);
        }else if(primitiveClass == char.class || primitiveClass == Character.class) {
            return ConfigurableWidget.fromCharMap(textRenderer, text, offset, width, height, key, primitive.getAsCharacter(), keyClass);
        }
        return ConfigurableWidget.fromStringMap(textRenderer, text, offset, width, height, key, primitive.getAsString(), keyClass);
    }
    
    public static ConfigurableWidget<String> fromLabel(TextRenderer textRenderer, Text text, int offset, int width, int height, String key) {
        return new ConfigurableWidget<>(textRenderer, text, offset, width, height, key, null, null, null, null);
    }

    public static <V> ConfigurableWidget<V> fromAddableEntryLabel(TextRenderer textRenderer, Text text, int offset, int width, int height, String key, Class<V> arrayReference) {
        return new ConfigurableWidget<>(textRenderer, text, offset, width, height, key, null, s -> null, arrayReference, null);
    }

    public static <V> ConfigurableWidget<V> fromAddableEntryLabel(TextRenderer textRenderer, Text text, int offset, int width, int height, String key, Map.Entry<Class<?>, Class<V>> mapReference) {
        return new ConfigurableWidget<>(textRenderer, text, offset, width, height, key, null, s -> null, null, mapReference);
    }

    public static <V> ConfigurableWidget<V> fromRemovableEntryLabel(TextRenderer textRenderer, Text text, int offset, int width, int height, String key, Class<V> arrayReference) {
        return new ConfigurableWidget<>(textRenderer, text, offset, width, height, key, null, null, arrayReference, null);
    }

}
