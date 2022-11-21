package me.drex.orderedplayerlist.config.sequence;

import net.minecraft.server.level.ServerPlayer;

import java.util.Comparator;

public interface Sequence {

    Comparator<ServerPlayer> comparator();

}
