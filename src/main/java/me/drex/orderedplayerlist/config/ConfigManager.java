package me.drex.orderedplayerlist.config;

import com.google.gson.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import me.drex.orderedplayerlist.OrderedPlayerList;
import me.drex.orderedplayerlist.config.sequence.Sequence;
import me.drex.orderedplayerlist.config.sequence.util.ComparisonMode;
import me.drex.orderedplayerlist.config.sequence.util.PlayerComparator;
import me.drex.orderedplayerlist.util.serializer.ComparisonModeSerializer;
import me.drex.orderedplayerlist.util.serializer.PlayerComparatorSerializer;
import me.drex.orderedplayerlist.util.serializer.SequenceSerializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {

    private static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .disableHtmlEscaping()
        .setStrictness(Strictness.LENIENT)
        .registerTypeAdapter(PlayerComparator.class, new PlayerComparatorSerializer())
        .registerTypeAdapter(ComparisonMode.class, new ComparisonModeSerializer())
        .registerTypeAdapter(Sequence.class, SequenceSerializer.INSTANCE)
        .registerTypeHierarchyAdapter(ResourceLocation.class, new RegistryUnawareCodecSerializer<>(ResourceLocation.CODEC))
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

    private record RegistryUnawareCodecSerializer<T>(Codec<T> codec) implements JsonSerializer<T>, JsonDeserializer<T> {
        @Override
        public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            try {
                return this.codec.decode(JsonOps.INSTANCE, json).getOrThrow().getFirst();
            } catch (Throwable e) {
                return null;
            }
        }

        @Override
        public JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
            try {
                return src != null ? this.codec.encodeStart(JsonOps.INSTANCE, src).getOrThrow() : JsonNull.INSTANCE;
            } catch (Throwable e) {
                return JsonNull.INSTANCE;
            }
        }
    }

}
