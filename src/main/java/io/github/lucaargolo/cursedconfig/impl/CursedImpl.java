package io.github.lucaargolo.cursedconfig.impl;

import io.github.lucaargolo.cursedconfig.CursedConfig;
import net.fabricmc.api.ModInitializer;

public class CursedImpl implements ModInitializer {

    @Override
    public void onInitialize() {
        new CursedConfig.Builder<ConfigImpl>()
                .setupClass(ConfigImpl.class)
                .setupDefault(new ConfigImpl())
                .setupPath("cursedconfig", "config1")
                .setupScreen("cursedconfig")
                .build();
        new CursedConfig.Builder<ConfigImpl>()
                .setupClass(ConfigImpl.class)
                .setupDefault(new ConfigImpl())
                .setupPath("cursedconfig", "config2")
                .setupScreen("cursedconfig")
                .build();
    }
}
