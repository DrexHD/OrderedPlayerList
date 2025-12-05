package me.drex.orderedplayerlist.config;

import me.drex.orderedplayerlist.config.sequence.MetadataSequence;
import me.drex.orderedplayerlist.config.sequence.util.ComparisonMode;
import me.drex.orderedplayerlist.config.sequence.util.PlayerComparator;

import java.util.ArrayList;

public class Config {

    public static Config INSTANCE = new Config();

    public int updateRate = 5;

    public PlayerComparator order = new PlayerComparator(new ArrayList<>() {{
        add(new MetadataSequence("weight", true, ComparisonMode.INTEGER));
//        add(new PlaceholderSequence(new Identifier("player:statistic"), "play_time", true, ComparisonMode.INTEGER));
//        add(new PlaceholderSequence(new Identifier("player:pos_y"), null, false, ComparisonMode.DOUBLE));
//        add(new PlaceholderSequence(new Identifier("player:statistic"), "deaths", false, ComparisonMode.INTEGER));
//        add(new PlaceholderSequence(new Identifier("player:name"), null, false, ComparisonMode.STRING));
    }});

}
