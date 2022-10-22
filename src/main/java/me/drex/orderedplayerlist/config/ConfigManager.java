package me.drex.orderedplayerlist.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.drex.orderedplayerlist.OrderedPlayerList;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().setLenient().create();
    private static final Path CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("ordered-playerlist.json");
    public static final ConfigManager INSTANCE = new ConfigManager();
    public Config config;

    private ConfigManager() {
    }

    private ConfigData loadData() throws IOException {
        ConfigData data;
        if (CONFIG_FILE.toFile().exists()) {
            data = GSON.fromJson(Files.readString(CONFIG_FILE), ConfigData.class);
        } else {
            data = new ConfigData();
            Files.writeString(CONFIG_FILE, GSON.toJson(data));
        }
        return data;
    }

    public boolean load() {
        ConfigData configData;
        boolean failed = false;
        try {
            configData = loadData();
            this.config = new Config(configData);
            return (!this.config.failed && !failed);
        } catch (Exception e) {
            OrderedPlayerList.LOGGER.error("An error occurred while loading the config data", e);
            if (config == null) {
                config = new Config(new ConfigData());
            }
            return false;
        }
    }

}
