package io.github.lucaargolo.latte.screen;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.lucaargolo.latte.LatteConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import java.util.ArrayList;
import java.util.List;


public class LatteScreen extends Screen {

    private final Screen previous;
    private final List<LatteConfig<?>> configs;
    private final List<ConfigurableListWidget> listWidgets;

    private int selectedList = 0;
    private ConfigurableListWidget listWidget;
    private ButtonWidget resetButton;
    private ButtonWidget saveButton;

    public LatteScreen(Screen previous, Text title, List<LatteConfig<?>> configs) {
        super(title);
        this.configs = configs;
        this.listWidgets = new ArrayList<>();
        configs.forEach(config -> this.listWidgets.add(new ConfigurableListWidget(config, MinecraftClient.getInstance(), 25)));
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

        this.saveButton = new ButtonWidget(this.width / 2 - 50, this.height - 28, 100, 20, new TranslatableText("screen.latte.save"), (button) -> this.saveList(this.listWidget.save()));
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
        configs.forEach(config -> this.listWidgets.add(new ConfigurableListWidget(config, MinecraftClient.getInstance(), 25)));
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

}
