package io.github.lucaargolo.latte.screen;

import com.google.gson.*;
import io.github.lucaargolo.latte.LatteConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TickableElement;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class ConfigurableListWidget extends EntryListWidget<ConfigurableWidget<?>> implements TickableElement {

    private final LatteConfig<?> config;

    private int offset = 0;
    private String lastElement = null;
    private boolean isArray = false, isMap = false, isObject = false;
    private final LinkedList<String> pushedLabels = new LinkedList<>();

    public ConfigurableListWidget(LatteConfig<?> config, MinecraftClient client) {
        super(client, 0, 0, 0, 0, 25);
        this.config = config;
        initElements("", null, config.getConfigClass(), config.getConfigClass(), config.getElement());
    }

    public void init(int width, int height, int top, int bottom) {
        this.width = width;
        this.height = height;
        this.top = top;
        this.bottom = bottom;
        this.right = width;
        for(ConfigurableWidget<?> configurable: children()) {
            configurable.init(width-20, 18);
        }
        setScrollAmount(getScrollAmount());
    }

    public boolean isResettable() {
        for(ConfigurableWidget<?> widget: children()) {
            if(widget.isResettable()) {
                return true;
            }
        }
        return false;
    }

    public void reset() {
        for(ConfigurableWidget<?> widget: children()) {
            widget.reset();
        }
    }

    public boolean isSavable() {
        for(ConfigurableWidget<?> widget: children()) {
            if(!widget.isSavable()) {
                return false;
            }
        }
        return true;
    }

    public JsonElement save() {
        return save(null);
    }

    public JsonElement save(Pair<Integer, Pair<String, JsonElement>> inject) {
        int index = 0, currentOffset = 0;
        LinkedList<JsonElement> jsonElementStack = new LinkedList<>();
        jsonElementStack.addLast(new JsonObject());
        for(ConfigurableWidget<?> widget: children()) {
            JsonObject widgetJsonObject = new JsonObject();
            widget.save(widgetJsonObject);
            Map.Entry<String, JsonElement> addedEntry = widgetJsonObject.entrySet().iterator().next();
            String key = addedEntry.getKey();
            JsonElement value = addedEntry.getValue();
            if(currentOffset > widget.getOffset()) {
                for(int x = currentOffset; x > widget.getOffset(); x--) {
                    jsonElementStack.removeLast();
                }
                currentOffset = widget.getOffset();
            }
            if(jsonElementStack.getLast() instanceof JsonObject) {
                ((JsonObject) jsonElementStack.getLast()).add(key, value);
            }else if(jsonElementStack.getLast() instanceof JsonArray) {
                ((JsonArray) jsonElementStack.getLast()).add(value);
            }
            if(value.isJsonObject() || value.isJsonArray()) {
                currentOffset++;
                jsonElementStack.addLast(value);
            }
            if(inject != null && index >= inject.getLeft() && (index + 1 == children().size() || children().get(index+1).getOffset() == children().get(inject.getLeft()).getOffset())) {
                Map.Entry<String, JsonElement> injectedEntry = new AbstractMap.SimpleEntry<>(inject.getRight().getLeft(), inject.getRight().getRight());
                String injectedKey = injectedEntry.getKey();
                JsonElement injectedValue = injectedEntry.getValue();
                if(currentOffset > children().get(inject.getLeft()).getOffset()+1) {
                    for(int x = currentOffset; x > children().get(inject.getLeft()).getOffset()+1; x--) {
                        jsonElementStack.removeLast();
                    }
                    currentOffset = children().get(inject.getLeft()).getOffset()+1;
                }
                if(jsonElementStack.getLast() instanceof JsonObject) {
                    ((JsonObject) jsonElementStack.getLast()).add(injectedKey, injectedValue);
                }else if(jsonElementStack.getLast() instanceof JsonArray) {
                    ((JsonArray) jsonElementStack.getLast()).add(injectedValue);
                }
                if(injectedValue.isJsonObject() || injectedValue.isJsonArray()) {
                    currentOffset++;
                    jsonElementStack.addLast(injectedValue);
                }
                inject = null;
            }
            index++;
        }
        System.out.println(jsonElementStack.getFirst().toString());
        return jsonElementStack.getFirst();
    }

    //TODO: If reloaded keep track of changed values
    public void reload(Pair<Integer, Pair<String, JsonElement>> inject) {
        JsonElement newElement = save(inject);
        children().clear();
        offset = 0;
        lastElement = null;
        isArray = false;
        isMap = false;
        isObject = false;
        pushedLabels.clear();
        initElements("", null, config.getConfigClass(), config.getConfigClass(), newElement);
        init(width, height, top, bottom);
    }

    @Override
    public void tick() {
        int size = children().size();
        Iterator<ConfigurableWidget<?>> widgetIterator = children().iterator();
        Pair<Integer, Pair<String, JsonElement>> injectable = null;
        int index = 0, removedOffset = -999;
        while (widgetIterator.hasNext()) {
            ConfigurableWidget<?> widget = widgetIterator.next();
            if(widget.isAdding()) {
                widget.setAdding(false);
                try {
                    Class<?> valueClass = widget.getValueClass();
                    Object value;
                    if(valueClass == int.class || valueClass == Integer.class) {
                        value = 0;
                    }else if(valueClass == short.class || valueClass == Short.class) {
                        value = 0;
                    }else if(valueClass == long.class || valueClass == Long.class) {
                        value = 0;
                    }else if(valueClass == double.class || valueClass == Double.class) {
                        value = 0;
                    }else if(valueClass == float.class || valueClass == Float.class) {
                        value = 0;
                    }else if(valueClass == byte.class || valueClass == Byte.class) {
                        value = 0;
                    }else if(valueClass == boolean.class || valueClass == Boolean.class) {
                        value = true;
                    }else if(valueClass == char.class || valueClass == Character.class) {
                        value = 'a';
                    }else{
                        value = valueClass.newInstance();
                    }
                    JsonElement valueElement = LatteConfig.GSON.toJsonTree(value);
                    injectable = new Pair<>(index, new Pair<>("", valueElement));

                    Class<?> keyClass = widget.getKeyClass();
                    if(keyClass != null) {
                        if(client.currentScreen instanceof LatteScreen) {
                            client.openScreen(new LatteScreen.AddKeyScreen((LatteScreen) client.currentScreen, keyClass, injectable));
                        }
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if(widget.isRemoved()) {
                removedOffset = widget.getOffset();
                widgetIterator.remove();
            }else{
                if(removedOffset+1 == widget.getOffset()) {
                    widgetIterator.remove();
                }else{
                    removedOffset = -999;
                    widget.tick();
                }
            }
            index++;
        }
        if(injectable != null) {
            reload(injectable);
        }else if(children().size() != size) {
            reload(null);
        }
    }

    private void initElements(String elementParent, String elementKey, Class<?> elementClass, Type elementType, JsonElement element) {
        if(lastElement == null || !lastElement.equals(elementParent)) {
            if(pushedLabels.lastIndexOf(elementParent) >= 0) {
                for(int x = pushedLabels.lastIndexOf(elementParent); x < pushedLabels.size(); x++) {
                    pushedLabels.removeLast();
                    offset--;
                }
            }else{
                pushedLabels.addLast(elementParent);
                if(lastElement != null) {
                    if(isArray || isMap) {
                        if(isObject) {
                            addEntry(ConfigurableWidget.fromRemovableEntryLabel(client.textRenderer, new LiteralText(elementParent), offset, width - 20, 18, elementParent, elementClass));
                        }else {
                            Class<?> innerKeyClass = null;
                            Class<?> innerValueClass = String.class;
                            if (elementType instanceof ParameterizedType) {
                                if(Collection.class.isAssignableFrom(elementClass)) {
                                    innerValueClass = (Class<?>) ((ParameterizedType) elementType).getActualTypeArguments()[0];
                                }else if(Map.class.isAssignableFrom(elementClass)) {
                                    innerKeyClass = (Class<?>) ((ParameterizedType) elementType).getActualTypeArguments()[0];
                                    innerValueClass = (Class<?>) ((ParameterizedType) elementType).getActualTypeArguments()[1];
                                }
                            } else if (elementClass.isArray()) {
                                innerValueClass = elementClass.getComponentType();
                            }
                            if(innerKeyClass != null) {
                                addEntry(ConfigurableWidget.fromAddableEntryLabel(client.textRenderer, new LiteralText(elementParent), offset, width - 20, 18, elementParent, new AbstractMap.SimpleEntry<>(innerKeyClass, innerValueClass)));
                            }else{
                                addEntry(ConfigurableWidget.fromAddableEntryLabel(client.textRenderer, new LiteralText(elementParent), offset, width - 20, 18, elementParent, innerValueClass));
                            }
                        }
                    }else{
                        addEntry(ConfigurableWidget.fromLabel(client.textRenderer, new LiteralText(elementParent), offset, width - 20, 18, elementParent));
                    }
                    offset++;
                }
            }
        }
        lastElement = elementParent;

        if(element.isJsonPrimitive()) {
            JsonPrimitive primitive = (JsonPrimitive) element;
            ConfigurableWidget<?> widget;
            try {
                if((isArray || isMap) && !isObject) {
                    if(isArray) {
                        Class<?> arrayClass = String.class;
                        if (elementType instanceof ParameterizedType && Collection.class.isAssignableFrom(elementClass)) {
                            arrayClass = (Class<?>) ((ParameterizedType) elementType).getActualTypeArguments()[0];
                        } else if (elementClass.isArray()) {
                            arrayClass = elementClass.getComponentType();
                        }
                        widget = ConfigurableWidget.fromPrimitiveArray(client.textRenderer, new LiteralText(elementKey), offset, width-20, 18, elementKey, primitive, arrayClass);
                    }else{
                        Class<?> mapKeyClass = String.class;
                        Class<?> mapValueClass = String.class;
                        if (elementType instanceof ParameterizedType) {
                            if(Map.class.isAssignableFrom(elementClass)) {
                                mapKeyClass = (Class<?>) ((ParameterizedType) elementType).getActualTypeArguments()[0];
                                mapValueClass = (Class<?>) ((ParameterizedType) elementType).getActualTypeArguments()[1];
                            }
                        }
                        widget = ConfigurableWidget.fromPrimitiveMap(client.textRenderer, new LiteralText(elementKey), offset, width-20, 18, elementKey, mapKeyClass, primitive, mapValueClass);
                    }
                }else{
                    Field innerField = elementClass.getDeclaredField(elementKey);
                    Class<?> fieldClass = innerField.getType();
                    widget = ConfigurableWidget.fromPrimitive(client.textRenderer, new LiteralText(elementKey), offset, width-20, 18, elementKey, primitive, fieldClass);
                }
            } catch (NoSuchFieldException ignored) {
                widget = ConfigurableWidget.fromPrimitive(client.textRenderer, new LiteralText(elementKey), offset, width-20, 18, elementKey, primitive, String.class);
            }
            addEntry(widget);
        }else if(element.isJsonObject()) {
            JsonObject object = (JsonObject) element;
            boolean lastIsArray = isArray;
            boolean lastIsMap = isMap;
            boolean lastIsObject = isObject;
            isObject = true;

            Class<?> innerElementClass = String.class;
            Type innerElementType = String.class;
            if(elementKey != null) {
                try {
                    if (elementType instanceof ParameterizedType) {
                        if(Collection.class.isAssignableFrom(elementClass)) {
                            innerElementClass = (Class<?>) ((ParameterizedType) elementType).getActualTypeArguments()[0];
                            innerElementType = ((ParameterizedType) elementType).getActualTypeArguments()[0];
                        }else if(Map.class.isAssignableFrom(elementClass)) {
                            innerElementClass = (Class<?>) ((ParameterizedType) elementType).getActualTypeArguments()[1];
                            innerElementType = ((ParameterizedType) elementType).getActualTypeArguments()[1];
                        }
                    } else if (elementClass.isArray()) {
                        innerElementClass = elementClass.getComponentType();
                        innerElementType = elementClass.getComponentType();
                    } else {
                        Field innerElementField = elementClass.getDeclaredField(elementKey);
                        innerElementClass = innerElementField.getType();
                        innerElementType = innerElementField.getGenericType();
                    }
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }
            }

            if(Map.class.isAssignableFrom(innerElementClass)) {
                isArray = false;
                isMap = true;
                isObject = false;
            }
            for(Map.Entry<String, JsonElement> objectEntry: object.entrySet()) {
                String innerElementKey = objectEntry.getKey();
                JsonElement innerElement = objectEntry.getValue();
                if(elementKey != null) {
                    initElements(elementKey, innerElementKey, innerElementClass, innerElementType, innerElement);
                }else{
                    initElements(elementParent, innerElementKey, elementClass, elementType, innerElement);
                }
            }
            if(object.size() == 0 && elementKey != null) {
                if(Map.class.isAssignableFrom(innerElementClass)) {
                    Class<?> innerKeyClass = String.class;
                    Class<?> innerValueClass = String.class;
                    if (elementType instanceof ParameterizedType) {
                        innerKeyClass = (Class<?>) ((ParameterizedType) elementType).getActualTypeArguments()[0];
                        innerValueClass = (Class<?>) ((ParameterizedType) elementType).getActualTypeArguments()[1];
                    }
                    addEntry(ConfigurableWidget.fromAddableEntryLabel(client.textRenderer, new LiteralText(elementParent), offset, width - 20, 18, elementParent, new AbstractMap.SimpleEntry<>(innerKeyClass, innerValueClass)));
                }
                addEntry(ConfigurableWidget.fromLabel(client.textRenderer, new LiteralText(elementKey), offset, width - 20, 18, elementKey));
            }
            isArray = lastIsArray;
            isMap = lastIsMap;
            isObject = lastIsObject;
        }else if(element.isJsonArray()) {
            JsonArray array = (JsonArray) element;
            Class<?> arrayClass = String[].class;
            Type arrayType = arrayClass;
            try {
                Field arrayField = elementClass.getDeclaredField(elementKey);
                arrayClass = arrayField.getType();
                arrayType = arrayField.getGenericType();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
            int arrayIndex = 0;
            boolean lastIsMap = isMap;
            boolean lastIsArray = isArray;
            boolean lastIsObject = isObject;
            isMap = false;
            isArray = true;
            isObject = false;
            for(JsonElement arrayElement: array) {
                initElements(elementKey, ""+arrayIndex, arrayClass, arrayType, arrayElement);
                arrayIndex++;
            }
            if(array.size() == 0) {
                Class<?> arrayElementClass = String.class;
                if(arrayType instanceof ParameterizedType && Collection.class.isAssignableFrom(arrayClass)) {
                    arrayElementClass = (Class<?>) ((ParameterizedType) arrayType).getActualTypeArguments()[0];
                }else if(arrayClass.isArray()) {
                    arrayElementClass = arrayClass.getComponentType();
                }
                addEntry(ConfigurableWidget.fromAddableEntryLabel(client.textRenderer, new LiteralText(elementKey), offset, width - 20, 18, elementKey, arrayElementClass));
            }
            isMap = lastIsMap;
            isArray = lastIsArray;
            isObject = lastIsObject;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.updateScrollingState(mouseX, mouseY, button);
        if (!this.isMouseOver(mouseX, mouseY)) {
            return false;
        } else {
            ConfigurableWidget<?> clickedWidget = null;
            for(ConfigurableWidget<?> widget: children()) {
                if(widget.mouseClicked(mouseX, mouseY, button)) {
                    clickedWidget = widget;
                }
            }
            if(clickedWidget != null) {
                this.setFocused(clickedWidget);
                if (button == 0) {
                    this.setDragging(true);
                }
                return true;
            }
            if (button == 0) {
                this.clickedHeader((int)(mouseX - (double)(this.left + this.width / 2 - this.getRowWidth() / 2)), (int)(mouseY - (double)this.top) + (int)this.getScrollAmount() - 4);
                return true;
            }

            return super.mouseClicked(mouseX, mouseY, button);
        }
    }

    @Override
    public int getRowLeft() {
        return 20;
    }

    @Override
    public int getRowWidth() {
        return this.width-20;
    }

    @Override
    protected int getScrollbarPositionX() {
        return this.width-5;
    }


    public Text getTitle() {
        return config.getTitle();
    }

}