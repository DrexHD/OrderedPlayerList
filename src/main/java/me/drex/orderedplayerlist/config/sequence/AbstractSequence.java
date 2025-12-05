package me.drex.orderedplayerlist.config.sequence;

import me.drex.orderedplayerlist.config.sequence.util.ComparisonMode;
import net.minecraft.server.level.ServerPlayer;

import java.util.Comparator;

public abstract class AbstractSequence implements Sequence {

    private final boolean reversed;
    private final ComparisonMode<?> mode;
    private final boolean ignoreCase = false;

    public AbstractSequence(boolean reversed, ComparisonMode<?> mode) {
        this.reversed = reversed;
        this.mode = mode;
    }

    @Override
    public Comparator<ServerPlayer> comparator() {
        Comparator<ServerPlayer> comparator = Comparator.comparing(player -> {
            String stringRepresentation = getStringRepresentation(player);
            if (ignoreCase) {
                stringRepresentation = stringRepresentation.toLowerCase();
            }
            return mode.getComparable(stringRepresentation);
        });
        if (reversed) comparator = comparator.reversed();
        return comparator;
    }

    protected abstract String getStringRepresentation(ServerPlayer player);

}
