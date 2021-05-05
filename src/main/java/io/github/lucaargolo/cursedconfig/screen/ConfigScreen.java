package io.github.lucaargolo.cursedconfig.screen;

import com.google.gson.JsonElement;
import io.github.lucaargolo.cursedconfig.CursedConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;


public class ConfigScreen extends Screen {

    private final Screen previous;
    private final List<CursedConfig<?>> configs;
    private final List<ConfigurableListWidget> listWidgets;

    private int selectedList = 0;
    private ConfigurableListWidget listWidget;
    private ButtonWidget resetButton;
    private ButtonWidget saveButton;

    public ConfigScreen(Screen previous, Text title, List<CursedConfig<?>> configs) {
        super(title);
        this.configs = configs;
        this.listWidgets = new ArrayList<>();
        configs.forEach(config -> this.listWidgets.add(new ConfigurableListWidget(config, MinecraftClient.getInstance())));
        this.previous = previous;
    }

    @Override
    protected void init() {
        children.clear();
        this.listWidget = listWidgets.get(selectedList);
        this.listWidget.init(this.width, this.height, 43, this.height - 32);
        this.addChild(this.listWidget);
        if(this.listWidgets.size() > 1) {
            int tabsWidth = (listWidgets.size()*74)-10;
            int tabsOffset = this.width/2 - tabsWidth/2;
            int configIndex = 0;
            for(ConfigurableListWidget listWidget: this.listWidgets) {
                int finalConfigIndex = configIndex;
                ButtonWidget newBtn = new ButtonWidget(tabsOffset+(configIndex*74), 18, 64, 20, listWidget.getTitle(), (button) -> this.selectList(finalConfigIndex));
                if(this.selectedList == configIndex) {
                    newBtn.active = false;
                }
                this.addButton(newBtn);
                configIndex++;
            }
        }
        this.resetButton = new ButtonWidget(this.width / 2 - 160, this.height - 28, 100, 20, new TranslatableText("controls.reset"), (button) -> this.listWidget.reset());
        this.resetButton.active = this.listWidget.isResettable();
        this.addButton(this.resetButton);

        this.saveButton = new ButtonWidget(this.width / 2 - 50, this.height - 28, 100, 20, new TranslatableText("screen.cursedconfig.save"), (button) -> this.saveList(this.listWidget.save()));
        this.saveButton.active = this.listWidget.isSavable();
        this.addButton(this.saveButton);

        this.addButton(new ButtonWidget(this.width / 2 + 60, this.height - 28, 100, 20, ScreenTexts.DONE, (button) -> this.onClose()));
    }

    private void selectList(int index) {
        this.selectedList = index;
        this.init();
    }

    private void saveList(JsonElement jsonElement) {
        this.configs.get(selectedList).save(jsonElement);
        listWidgets.clear();
        configs.forEach(config -> this.listWidgets.add(new ConfigurableListWidget(config, MinecraftClient.getInstance())));
        init();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        this.listWidget.render(matrices, mouseX, mouseY, delta);
        if(this.listWidgets.size() > 1) {
            drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 8, 0xffffff);
        }else{
            drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 18, 0xffffff);
        }
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void tick() {
        this.listWidget.tick();
        resetButton.active = this.listWidget.isResettable();
        saveButton.active = this.listWidget.isResettable() && this.listWidget.isSavable();
    }

    public void onClose() {
        if(this.client != null) {
            this.client.openScreen(this.previous);
        }
    }

    public static class AddKeyScreen extends Screen {

        private final ConfigScreen previous;
        private final Function<String, Object> validator;
        private final Pair<Integer, Pair<String, JsonElement>> injected;
        private TextFieldWidget textField;
        private ButtonWidget doneButton;

        public AddKeyScreen(ConfigScreen previous, Class<?> keyClass, Pair<Integer, Pair<String, JsonElement>> injected) {
            super(new TranslatableText("screen.cursedconfig.add_entry"));
            this.previous = previous;

            if(keyClass == int.class || keyClass == Integer.class) {
                validator = s -> { try { return Integer.parseInt(s); } catch (Exception ignored) { return null; } };
            }else if(keyClass == short.class || keyClass == Short.class) {
                validator = s -> { try { return Short.parseShort(s); } catch (Exception ignored) { return null; } };
            }else if(keyClass == long.class || keyClass == Long.class) {
                validator = s -> { try { return Long.parseLong(s); } catch (Exception ignored) { return null; } };
            }else if(keyClass == double.class || keyClass == Double.class) {
                validator = s -> { try { return Double.parseDouble(s); } catch (Exception ignored) { return null; } };
            }else if(keyClass == float.class || keyClass == Float.class) {
                validator = s -> { try { return Float.parseFloat(s); } catch (Exception ignored) { return null; } };
            }else if(keyClass == byte.class || keyClass == Byte.class) {
                validator = s -> { try { return Byte.parseByte(s); } catch (Exception ignored) { return null; } };
            }else if(keyClass == boolean.class || keyClass == Boolean.class) {
                validator = s -> s.equals("true") || s.equals("false");
            }else if(keyClass == char.class || keyClass == Character.class) {
                validator = s -> s.toCharArray().length == 1;
            }else{
                validator = s -> "";
            }

            this.injected = injected;
        }

        @Override
        public void init() {
            children.clear();
            this.textField = this.addChild(new TextFieldWidget(textRenderer, this.width / 2 - 50, this.height / 2 - 10, 100, 20, new LiteralText("")));
            this.doneButton = this.addButton(new ButtonWidget(this.width / 2 - 50, this.height / 2 + 20, 100, 20, ScreenTexts.DONE, (button) -> this.onClose()));
        }

        @Override
        public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            this.renderBackground(matrices);
            this.textField.render(matrices, mouseX, mouseY, delta);
            drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, this.height / 2 - 30, 0xffffff);
            if(validator.apply(textField.getText()) != null) {
                textField.setEditableColor(14737632);
                doneButton.active = true;
            }else{
                textField.setEditableColor(0xFFFF0000);
                doneButton.active = false;
            }
            super.render(matrices, mouseX, mouseY, delta);
        }

        @Override
        public void tick() {
            this.textField.tick();
        }

        public void onClose() {
            if(this.client != null) {
                String newKey = textField.getText();
                Pair<Integer, Pair<String, JsonElement>> newInjected = new Pair<>(injected.getLeft(), new Pair<>(newKey, injected.getRight().getRight()));
                this.previous.listWidget.reload(newInjected);
                this.client.openScreen(this.previous);
            }
        }
    }

}
