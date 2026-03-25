package me.drex.orderedplayerlist.config.sequence;

import eu.pb4.placeholders.api.Placeholder;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.ServerPlaceholderContext;
import me.drex.orderedplayerlist.config.sequence.util.ComparisonMode;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceholderSequence extends AbstractSequence {

    public static final Identifier ID = Identifier.withDefaultNamespace("placeholder");
    private final Identifier placeholder;
    private final String argument;

    public PlaceholderSequence(@NotNull Identifier placeholder, @Nullable String argument, boolean reversed, ComparisonMode<?> mode) {
        super(reversed, mode);
        this.placeholder = placeholder;
        this.argument = argument;
    }

    @Override
    protected String getStringRepresentation(ServerPlayer player) {
        Placeholder<ServerPlaceholderContext, ?> placeholder = Placeholders.getServerPlaceholder(this.placeholder);
        if (placeholder == null) return "";
        return placeholder.onPlaceholderRequest(ServerPlaceholderContext.of(player), this.argument).component().getString();
    }
}
