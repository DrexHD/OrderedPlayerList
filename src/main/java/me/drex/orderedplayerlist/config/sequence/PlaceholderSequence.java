package me.drex.orderedplayerlist.config.sequence;

import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import me.drex.orderedplayerlist.config.sequence.util.ComparisonMode;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceholderSequence extends AbstractSequence {

    public static final ResourceLocation ID = ResourceLocation.withDefaultNamespace("placeholder");
    private final ResourceLocation placeholder;
    private final String argument;

    public PlaceholderSequence(@NotNull ResourceLocation placeholder, @Nullable String argument, boolean reversed, ComparisonMode<?> mode) {
        super(reversed, mode);
        this.placeholder = placeholder;
        this.argument = argument;
    }

    @Override
    protected String getStringRepresentation(ServerPlayer player) {
        return Placeholders.parsePlaceholder(this.placeholder, this.argument, PlaceholderContext.of(player)).string();
    }
}
