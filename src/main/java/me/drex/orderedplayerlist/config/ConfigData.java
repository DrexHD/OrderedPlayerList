package me.drex.orderedplayerlist.config;

import java.util.LinkedHashMap;

public class ConfigData {

    public boolean debug = false;
    public int updateRate = 5;
    LinkedHashMap<String, String> ordering = defaultOrdering();

    static LinkedHashMap<String, String> defaultOrdering() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("reversed_metadata_int", "weight");
        map.put("placeholder_string", "%player:name%");
        return map;
    }

}
