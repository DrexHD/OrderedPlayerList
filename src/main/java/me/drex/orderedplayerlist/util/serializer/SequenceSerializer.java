package me.drex.orderedplayerlist.util.serializer;

import com.google.gson.*;
import me.drex.orderedplayerlist.config.sequence.MetadataSequence;
import me.drex.orderedplayerlist.config.sequence.PlaceholderSequence;
import me.drex.orderedplayerlist.config.sequence.Sequence;
import net.minecraft.resources.Identifier;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public final class SequenceSerializer implements JsonSerializer<Sequence>, JsonDeserializer<Sequence> {

    public static final SequenceSerializer INSTANCE = new SequenceSerializer();
    public static final Map<Identifier, Class<? extends Sequence>> ID_TO_CLASS = new HashMap<>();
    public static final Map<Class<? extends Sequence>, Identifier> CLASS_TO_ID = new HashMap<>();

    static {
        register(PlaceholderSequence.ID, PlaceholderSequence.class);
        register(MetadataSequence.ID, MetadataSequence.class);
    }

    private SequenceSerializer() {}

    @Override
    public Sequence deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        try {
            JsonElement json = jsonElement.getAsJsonObject().get("type");
            if (json != null) {
                Identifier resourceLocation = context.deserialize(json, Identifier.class);
                Class<? extends Sequence> clazz = ID_TO_CLASS.get(resourceLocation);
                if (clazz != null) {
                    return context.deserialize(jsonElement, clazz);
                } else {
                    throw new IllegalArgumentException("Invalid sequence type \"" + json.getAsString() + "\"!");
                }
            } else {
                throw new IllegalArgumentException("Missing sequence type!");
            }
        } catch (Throwable t) {
            throw new JsonParseException(t);
        }
    }

    @Override
    public JsonElement serialize(Sequence src, Type type, JsonSerializationContext context) {
        Identifier resourceLocation = CLASS_TO_ID.get(src.getClass());
        if (resourceLocation != null) {
            JsonObject jsonObject = context.serialize(src).getAsJsonObject();
            jsonObject.addProperty("type", resourceLocation.getNamespace().equals(Identifier.DEFAULT_NAMESPACE) ? resourceLocation.getPath() : resourceLocation.toString());
            return jsonObject;
        } else {
            throw new JsonParseException("Unknown sequence \"" + src + "\", missing register()!");
        }
    }

    public static <T extends Sequence> void register(Identifier resourceLocation, Class<T> type) {
        ID_TO_CLASS.put(resourceLocation, type);
        CLASS_TO_ID.put(type, resourceLocation);
    }
}
