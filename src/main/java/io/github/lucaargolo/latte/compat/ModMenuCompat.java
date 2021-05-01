package io.github.lucaargolo.latte.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import io.github.lucaargolo.latte.LatteConfig;
import io.github.lucaargolo.latte.screen.LatteScreen;

import java.util.HashMap;
import java.util.Map;

public class ModMenuCompat implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return (previous) -> new LatteScreen(previous, LatteConfig.configsForScreens.get("latte").getLeft(), LatteConfig.configsForScreens.get("latte").getRight());
    }

    @Override
    public Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
        HashMap<String, ConfigScreenFactory<?>> providedScreens = new HashMap<>();
        LatteConfig.configsForScreens.forEach((id, pair) -> providedScreens.put(id, (previous) -> new LatteScreen(previous, pair.getLeft(), pair.getRight())));
        return providedScreens;
    }

}
