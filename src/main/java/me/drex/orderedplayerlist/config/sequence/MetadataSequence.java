package me.drex.orderedplayerlist.config.sequence;

import me.drex.orderedplayerlist.config.sequence.util.ComparisonMode;
import me.lucko.fabric.api.permissions.v0.Options;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class MetadataSequence extends AbstractSequence {

    public static final ResourceLocation ID = new ResourceLocation("metadata");
    private final String key;

    public MetadataSequence(@NotNull String key, boolean reversed, ComparisonMode<?> mode) {
        super(reversed, mode);
        this.key = key;
    }

    public String key() {
        return key;
    }


    @Override
    protected String getStringRepresentation(ServerPlayer player) {
        return Options.get(player, key, "");
    }
}
