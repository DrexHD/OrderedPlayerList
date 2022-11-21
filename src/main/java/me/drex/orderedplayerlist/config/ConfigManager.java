package me.drex.orderedplayerlist.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.drex.orderedplayerlist.OrderedPlayerList;
import me.drex.orderedplayerlist.config.sequence.Sequence;
import me.drex.orderedplayerlist.config.sequence.util.ComparisonMode;
import me.drex.orderedplayerlist.config.sequence.util.PlayerComparator;
import me.drex.orderedplayerlist.util.serializer.ComparisonModeSerializer;
import me.drex.orderedplayerlist.util.serializer.PlayerComparatorSerializer;
import me.drex.orderedplayerlist.util.serializer.SequenceSerializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().setLenient()
            .registerTypeAdapter(PlayerComparator.class, new PlayerComparatorSerializer())
            .registerTypeAdapter(ComparisonMode.class, new ComparisonModeSerializer())
            .registerTypeAdapter(Sequence.class, SequenceSerializer.INSTANCE)
            .registerTypeHierarchyAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
            .create();
    private static final Path CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("ordered-playerlist.json");

    private ConfigManager() {
    }

    public static boolean load() {
        if (CONFIG_FILE.toFile().exists()) {
            try {
                Config.INSTANCE = GSON.fromJson(Files.readString(CONFIG_FILE), Config.class);
                return true;
            } catch (Exception ex) {
                OrderedPlayerList.LOGGER.error("Failed to load config file!", ex);
                return false;
            }
        } else {
            try {
                Files.writeString(CONFIG_FILE, GSON.toJson(Config.INSTANCE));
                return true;
            } catch (Exception ex) {
                OrderedPlayerList.LOGGER.error("Failed to save config file!", ex);
                return false;
            }
        }
    }

}
