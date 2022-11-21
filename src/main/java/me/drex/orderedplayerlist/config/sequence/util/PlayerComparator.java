package me.drex.orderedplayerlist.config.sequence.util;

import com.google.common.collect.ComparisonChain;
import me.drex.orderedplayerlist.config.sequence.Sequence;
import net.minecraft.server.level.ServerPlayer;

import java.util.Comparator;
import java.util.List;

public class PlayerComparator {

    private final Comparator<ServerPlayer> comparator;
    private final List<Sequence> sequences;

    public PlayerComparator(List<Sequence> sequences) {
        this.sequences = sequences;
        this.comparator = (player1, player2) -> {
            ComparisonChain comparisonChain = ComparisonChain.start();
            for (Sequence sequence : this.sequences) {
                comparisonChain = comparisonChain.compare(player1, player2, sequence.comparator());
            }
            return comparisonChain.result();
        };
    }

    public Comparator<ServerPlayer> comparator() {
        return comparator;
    }

    public List<Sequence> sequences() {
        return sequences;
    }
}
