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
        //In case there's a screen implementation for this mod we need to add it here.
        if(CursedConfig.configsForScreens.containsKey("cursedconfig")) {
            return (previous) -> new ConfigScreen(previous, CursedConfig.configsForScreens.get("cursedconfig").getLeft(), CursedConfig.configsForScreens.get("cursedconfig").getRight());
        }
        return ModMenuApi.super.getModConfigScreenFactory();
    }

    @Override
    public Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
        HashMap<String, ConfigScreenFactory<?>> providedScreens = new HashMap<>();
        CursedConfig.configsForScreens.forEach((id, pair) -> providedScreens.put(id, (previous) -> new ConfigScreen(previous, pair.getLeft(), pair.getRight())));
        return providedScreens;
    }

}
