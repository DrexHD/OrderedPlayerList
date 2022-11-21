package me.drex.orderedplayerlist.util.serializer;

import com.google.gson.*;
import me.drex.orderedplayerlist.config.sequence.util.ComparisonMode;

import java.lang.reflect.Type;

public class ComparisonModeSerializer implements JsonSerializer<ComparisonMode<?>>, JsonDeserializer<ComparisonMode<?>> {

    @Override
    public ComparisonMode<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String id = json.getAsString();
        ComparisonMode<?> mode = ComparisonMode.COMPARISON_MODES.get(id);
        if (mode != null) {
            return mode;
        } else {
            throw new JsonParseException("Invalid comparison mode \"" + id + "\"!");
        }
    }

    @Override
    public JsonElement serialize(ComparisonMode<?> src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.id());
    }
}
