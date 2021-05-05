package io.github.lucaargolo.cursedconfig.api.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import io.github.lucaargolo.cursedconfig.api.CursedConfig;
import io.github.lucaargolo.cursedconfig.api.screen.ConfigScreen;

import java.util.HashMap;
import java.util.Map;

public class ModMenuCompat implements ModMenuApi {

    @Override
    public Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
        HashMap<String, ConfigScreenFactory<?>> providedScreens = new HashMap<>();
        CursedConfig.configsForScreens.forEach((id, pair) -> providedScreens.put(id, (previous) -> new ConfigScreen(previous, pair.getLeft(), pair.getRight())));
        return providedScreens;
    }

}
