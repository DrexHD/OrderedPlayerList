package me.drex.orderedplayerlist.config;

import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.node.LiteralNode;
import me.drex.orderedplayerlist.OrderedPlayerList;
import me.lucko.fabric.api.permissions.v0.Options;
import net.minecraft.server.level.ServerPlayer;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public enum OrderLookup {

    METADATA("metadata", (arg) -> Options.get(arg.player(), arg.argument(), "")),
    PLACEHOLDER("placeholder", (arg) -> Placeholders.parseNodes(new LiteralNode(arg.argument())).toText(ParserContext.of(PlaceholderContext.KEY, PlaceholderContext.of(arg.player())), true).getString());

    public static final String REVERSED = "reversed_";
    private final String id;
    private final Function<PlayerListOrderArgument, String> getResultFunction;
    static final Set<ParsedComparableType<?>> COMPARABLE_TYPES = new HashSet<>();

    static {
        COMPARABLE_TYPES.add(new ParsedComparableType<>("boolean", Boolean::parseBoolean, false));
        COMPARABLE_TYPES.add(new ParsedComparableType<>("int", Integer::parseInt, 0));
        COMPARABLE_TYPES.add(new ParsedComparableType<>("long", Long::parseLong, 0L));
        COMPARABLE_TYPES.add(new ParsedComparableType<>("float", Float::parseFloat, 0F));
        COMPARABLE_TYPES.add(new ParsedComparableType<>("double", Double::parseDouble, 0D));
        COMPARABLE_TYPES.add(new ParsedComparableType<>("string", Function.identity(), ""));
    }

    OrderLookup(String id, Function<PlayerListOrderArgument, String> getResultFunction) {
        this.id = id;
        this.getResultFunction = getResultFunction;
    }

    public String id() {
        return id;
    }

    public Function<PlayerListOrderArgument, String> getResultFunction() {
        return getResultFunction;
    }

    public static Comparator<ServerPlayer> getComparator(String fullOrderId, String argument) {
        for (ParsedComparableType<? extends Comparable<?>> comparable : COMPARABLE_TYPES) {
            if (comparable.shouldParse(fullOrderId)) {
                return comparable.getComparator(fullOrderId, argument);
            }
        }
        throw new IllegalArgumentException("Invalid orderId %s, needs to end with \"_int\", \"_string\" or \"_double\"".formatted(fullOrderId));
    }

    record PlayerListOrderArgument(ServerPlayer player, String argument) {

    }

    record ParsedComparableType<T extends Comparable<T>>(String suffix, Function<String, ? extends T> parser, T fallback) {

        boolean shouldParse(String fullOrderId) {
            return fullOrderId.endsWith("_" + this.suffix());
        }

        private Comparator<ServerPlayer> getComparator(String orderId, String argument) {
            boolean reversed = false;
            if (orderId.startsWith(REVERSED)) {
                orderId = orderId.substring(REVERSED.length());
                reversed = true;
            }
            String id = orderId.substring(0, orderId.length() - 1 - suffix().length());
            for (OrderLookup orderLookup : OrderLookup.values()) {
                if (orderLookup.id().equals(id)) {
                    Comparator<ServerPlayer> comparator = Comparator.comparing((player) -> {
                        String result = orderLookup.getResultFunction().apply(new PlayerListOrderArgument(player, argument));
                        try {
                            return parser().apply(result);
                        } catch (IllegalArgumentException e) {
                            if (ConfigManager.INSTANCE.config.debug) OrderedPlayerList.LOGGER.info("An error occurred while parsing {}, using fallback {}", result, fallback(), e);
                            return fallback();
                        }
                    });
                    if (reversed) comparator = comparator.reversed();
                    return comparator;
                }
            }
            throw new IllegalArgumentException("Invalid order lookup id %s, needs to be \"reversed_\" (optional) then \"metadata\" or \"placeholder\" followed by \"_int\", \"_double\" or \"_string\"! Examples: \"reversed_metadata_int\", \"placeholder_string\"".formatted(orderId));
        }
    }

}
