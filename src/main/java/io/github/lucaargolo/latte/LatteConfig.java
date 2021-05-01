package io.github.lucaargolo.latte;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class LatteConfig<C> {

    public static HashMap<String, Pair<Text, ArrayList<LatteConfig<?>>>> configsForScreens = new HashMap<>();

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final Logger LOGGER = LogManager.getLogger("Latte");
    private static final JsonParser JSON_PARSER = new JsonParser();

    private final File configFile;
    private final Text configTitle;
    private final Class<C> configClass;
    private final C config;
    private JsonElement configElement;

    private LatteConfig(File configFile, Text configTitle, Class<C> configClass, C config, JsonElement configElement) {
        this.configFile = configFile;
        this.configTitle = configTitle;
        this.configClass = configClass;
        this.config = config;
        this.configElement = configElement;
    }

    public Text getTitle() {
        return configTitle;
    }

    public Class<C> getConfigClass() {
        return configClass;
    }

    public C getConfig() {
        return config;
    }

    public JsonElement getElement() {
        return configElement;
    }

    public void save(JsonElement element) {
        LOGGER.info("Saving config file found at: "+configFile+"...");
        String jsonString = GSON.toJson(element);
        try (PrintWriter out = new PrintWriter(configFile)) {
            out.println(jsonString);
            configElement = element;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static class Builder<C> {

        Text configTitle = null;
        Class<C> configClass = null;
        C defaultConfig = null;
        String[] configPath = null;
        String modIdForScreen = null;

        public Builder<C> setupTitle(Text configTitle) {
            this.configTitle = configTitle;
            return this;
        }

        public Builder<C> setupClass(Class<C> configClass) {
            this.configClass = configClass;
            return this;
        }

        public Builder<C> setupDefault(C defaultConfig) {
            this.defaultConfig = defaultConfig;
            return this;
        }

        public Builder<C> setupPath(String... configPath) {
            this.configPath = configPath;
            this.configTitle = new LiteralText(configPath[configPath.length-1]);
            return this;
        }

        public Builder<C> setupScreen(String id) {
            this.modIdForScreen = id;
            return this;
        }

        public LatteConfig<C> build() {
            if(configClass == null) {
                throw new NullPointerException("Config class can't be null");
            }else if(defaultConfig == null){
                throw new NullPointerException("Default config can't be null");
            }else if(configPath == null || configPath.length == 0) {
                throw new NullPointerException("Config path can't be null nor empty");
            }else {

                Path configDir = FabricLoader.getInstance().getConfigDir();

                StringBuilder folderPath = new StringBuilder();
                for(int x = 0; x < configPath.length-1; x++) {
                    folderPath.append(File.separator).append(configPath[x]);
                }
                File folderFile = new File(configDir + folderPath.toString());
                if(folderFile.mkdirs()) {
                    LOGGER.info("Created folders for config files at: "+folderFile);
                }

                StringBuilder filePath = new StringBuilder();
                for (String s : configPath) {
                    filePath.append(File.separator).append(s);
                }
                File configFile = new File(configDir + filePath.toString() + ".json");

                LatteConfig<C> latteConfig = null;

                try {
                    if (configFile.createNewFile()) {
                        LOGGER.info("No config file found at: "+configFile+". Creating default one...");
                        String jsonString = GSON.toJson(defaultConfig);
                        try (PrintWriter out = new PrintWriter(configFile)) {
                            out.println(jsonString);
                        }
                        latteConfig = new LatteConfig<>(configFile, configTitle, configClass, defaultConfig, JSON_PARSER.parse(jsonString));
                    } else {
                        LOGGER.info("A config file was found at: "+configFile+". Loading it..");
                        String jsonString = new String(Files.readAllBytes(configFile.toPath()));
                        C loadedConfig = GSON.fromJson(jsonString, configClass);
                        if(loadedConfig == null) {
                            throw new NullPointerException("The config file was empty.");
                        }
                        latteConfig = new LatteConfig<>(configFile, configTitle, configClass, loadedConfig, JSON_PARSER.parse(jsonString));
                    }
                }catch (Exception exception) {
                    LOGGER.error("There was an error creating/loading the config file at "+configFile+"!", exception);
                }

                if(latteConfig == null) {
                    String jsonString = GSON.toJson(GSON.toJson(defaultConfig));
                    LOGGER.warn("Failed to load config file at: " + configFile + ". Defaulting to original one.");
                    latteConfig = new LatteConfig<>(configFile, configTitle, configClass, defaultConfig, JSON_PARSER.parse(jsonString));
                }

                if(modIdForScreen != null) {
                    Pair<Text, ArrayList<LatteConfig<?>>> registeredConfigs = configsForScreens.get(modIdForScreen);
                    if(registeredConfigs == null) {
                        registeredConfigs = new Pair<>(configTitle, new ArrayList<>());
                    }
                    registeredConfigs.getRight().add(latteConfig);
                    configsForScreens.put(modIdForScreen, registeredConfigs);
                }

                return latteConfig;
            }
        }

    }


}
