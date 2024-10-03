package me.drex.orderedplayerlist.util;

import me.drex.orderedplayerlist.config.Config;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

import java.util.*;

public class OrderedPlayerListManager {

    private OrderedPlayerListManager() {
    }

    public static void init() {
        ServerTickEvents.START_SERVER_TICK.register(OrderedPlayerListManager::onTick);
    }

    private static void onTick(MinecraftServer server) {
        if (Config.INSTANCE.updateRate > 0 && server.getTickCount() % Config.INSTANCE.updateRate == 0) {
            updateAll(server.getPlayerList(), true);
        }
    }
    public static void updateAll(PlayerList playerList, boolean sendPacket) {
        List<ServerPlayer> players = playerList.getPlayers();
        players = new ArrayList<>(players);
        players.sort(Config.INSTANCE.order.comparator());
        int tabListOrder = players.size();
        for (ServerPlayer player : players) {
            ((IServerPlayer) player).orderedPlayerList$setTabListOrder(tabListOrder);
            tabListOrder--;
        }
        if (sendPacket) {
            playerList.broadcastAll(new ClientboundPlayerInfoUpdatePacket(EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LIST_ORDER), players));
        }
    }

}
