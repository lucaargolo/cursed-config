package io.github.lucaargolo.cursedconfig.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import io.github.lucaargolo.cursedconfig.CursedConfig;
import io.github.lucaargolo.cursedconfig.screen.ConfigScreen;

import java.util.HashMap;
import java.util.Map;

public class ModMenuCompat implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return (previous) -> new ConfigScreen(previous, CursedConfig.configsForScreens.get("latte").getLeft(), CursedConfig.configsForScreens.get("latte").getRight());
    }

    @Override
    public Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
        HashMap<String, ConfigScreenFactory<?>> providedScreens = new HashMap<>();
        CursedConfig.configsForScreens.forEach((id, pair) -> providedScreens.put(id, (previous) -> new ConfigScreen(previous, pair.getLeft(), pair.getRight())));
        return providedScreens;
    }

}
