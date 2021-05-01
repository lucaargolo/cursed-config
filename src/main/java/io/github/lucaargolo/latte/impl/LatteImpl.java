package io.github.lucaargolo.latte.impl;

import io.github.lucaargolo.latte.LatteConfig;
import net.fabricmc.api.ModInitializer;

public class LatteImpl implements ModInitializer {

    @Override
    public void onInitialize() {
        new LatteConfig.Builder<ConfigImpl>()
                .setupClass(ConfigImpl.class)
                .setupDefault(new ConfigImpl())
                .setupPath("latte", "latte")
                .setupScreen("latte")
                .build();
        new LatteConfig.Builder<ConfigImpl>()
                .setupClass(ConfigImpl.class)
                .setupDefault(new ConfigImpl())
                .setupPath("latte", "latte2")
                .setupScreen("latte")
                .build();
    }
}
