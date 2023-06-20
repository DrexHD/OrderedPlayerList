package me.drex.orderedplayerlist.config;

import me.drex.orderedplayerlist.config.sequence.MetadataSequence;
import me.drex.orderedplayerlist.config.sequence.PlaceholderSequence;
import me.drex.orderedplayerlist.config.sequence.util.ComparisonMode;
import me.drex.orderedplayerlist.config.sequence.util.PlayerComparator;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;

public class Config {

    public static Config INSTANCE = new Config();

    public int updateRate = 5;

    public boolean displayPrefix = true;

    public boolean displaySuffix = true;

    public PlayerComparator order = new PlayerComparator(new ArrayList<>() {{
        add(new MetadataSequence("weight", true, ComparisonMode.INTEGER));
        add(new PlaceholderSequence(new ResourceLocation("player:statistic"), "play_time", true, ComparisonMode.INTEGER));
        add(new PlaceholderSequence(new ResourceLocation("player:pos_y"), null, false, ComparisonMode.DOUBLE));
        add(new PlaceholderSequence(new ResourceLocation("player:statistic"), "deaths", false, ComparisonMode.INTEGER));
        add(new PlaceholderSequence(new ResourceLocation("player:name"), null, false, ComparisonMode.STRING));
    }});

}
