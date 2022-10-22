package me.drex.orderedplayerlist.config;

import com.google.common.collect.ComparisonChain;
import me.drex.orderedplayerlist.OrderedPlayerList;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;

public class Config {

    public final boolean debug;
    public final int updateRate;
    public final Comparator<ServerPlayer> comparator;
    protected final boolean failed;

    public Config(ConfigData data) {
        debug = data.debug;
        updateRate = data.updateRate;
        boolean failed = false;
        List<Comparator<ServerPlayer>> comparators = new LinkedList<>();
        for (Map.Entry<String, String> entry : data.ordering.entrySet()) {
            try {
                comparators.add(OrderLookup.getComparator(entry.getKey(), entry.getValue()));
            } catch (Exception e) {
                failed = true;
                OrderedPlayerList.LOGGER.error("Order entry with id \"{}\" could not be parsed, ignoring", entry.getKey(), e);
            }
        }
        this.failed = failed;

        comparator = (o1, o2) -> {
            ComparisonChain comparisonChain = ComparisonChain.start();
            for (Comparator<ServerPlayer> playerComparator : comparators) {
                comparisonChain = comparisonChain.compare(o1, o2, playerComparator);
            }
            return comparisonChain.result();
        };
    }


}
