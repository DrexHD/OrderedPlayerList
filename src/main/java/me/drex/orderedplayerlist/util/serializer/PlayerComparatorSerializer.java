package me.drex.orderedplayerlist.util.serializer;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import me.drex.orderedplayerlist.config.sequence.Sequence;
import me.drex.orderedplayerlist.config.sequence.util.PlayerComparator;

import java.lang.reflect.Type;
import java.util.List;

public class PlayerComparatorSerializer implements JsonSerializer<PlayerComparator>, JsonDeserializer<PlayerComparator> {

    private static final Type SEQUENCE_LIST_TYPE = new TypeToken<List<Sequence>>() {
    }.getType();

    @Override
    public PlayerComparator deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        return new PlayerComparator(context.deserialize(jsonElement, SEQUENCE_LIST_TYPE));
    }

    @Override
    public JsonElement serialize(PlayerComparator playerComparator, Type type, JsonSerializationContext context) {

        return context.serialize(playerComparator.sequences(), SEQUENCE_LIST_TYPE);
    }
}
