package me.drex.orderedplayerlist.config.sequence.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class ComparisonMode<T extends Comparable<T>> {

    public static final Map<String, ComparisonMode<?>> COMPARISON_MODES = new HashMap<>();

    public static final ComparisonMode<Boolean> BOOLEAN = new ComparisonMode<>("boolean", false, Boolean::parseBoolean);
    public static final ComparisonMode<Integer> INTEGER = new ComparisonMode<>("integer", 0, Integer::parseInt);
    public static final ComparisonMode<Long> LONG = new ComparisonMode<>("long", 0L, Long::parseLong);
    public static final ComparisonMode<Double> DOUBLE = new ComparisonMode<>("double", 0D, Double::parseDouble);
    public static final ComparisonMode<String> STRING = new ComparisonMode<>("string", "", Function.identity());

    private String id;
    private final T defaultValue;
    private final Function<String, T> function;

    private ComparisonMode(String id, T defaultValue, Function<String, T> function) {
        this.id = id;
        COMPARISON_MODES.put(id.toLowerCase(), this);
        this.defaultValue = defaultValue;
        this.function = function;
    }
    
    public String id() {
        return id;
    }

    public T getComparable(String input) {
        try {
            return function.apply(input);
        } catch (IllegalArgumentException ignored) {
            return defaultValue;
        }
    }

}
