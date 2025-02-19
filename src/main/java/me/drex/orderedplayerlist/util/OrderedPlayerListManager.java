package me.drex.orderedplayerlist.util;

import me.drex.orderedplayerlist.config.Config;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

import java.util.ArrayList;
import java.util.List;

public class OrderedPlayerListManager {

    private static final List<DummyTeam> currentTeams = new ArrayList<>();

    private OrderedPlayerListManager() {
    }

    public static void init() {
        ServerTickEvents.START_SERVER_TICK.register(OrderedPlayerListManager::onTick);
    }

    private static void onTick(MinecraftServer server) {
        if (Config.INSTANCE.updateRate > 0 && server.getTickCount() % Config.INSTANCE.updateRate == 0) {
            updateAll(server.getPlayerList());
        }
    }

    public static void updateAll(PlayerList playerList) {
        List<ServerPlayer> sortedPlayers = playerList.getPlayers();
        sortedPlayers = new ArrayList<>(sortedPlayers);
        sortedPlayers.sort(Config.INSTANCE.order.comparator());

        List<ClientboundSetPlayerTeamPacket> modificationPackets = new ArrayList<>();

        boolean updateAll = false;
        if (currentTeams.size() == sortedPlayers.size()) {
            for (int i = 0; i < currentTeams.size(); i++) {
                ServerPlayer player = sortedPlayers.get(i);
                DummyTeam dummyTeam = currentTeams.get(i);
                if (!player.getUUID().equals(dummyTeam.player)) {
                    updateAll = true;
                    break;
                }
                if (dummyTeam.update(player)) {
                    modificationPackets.add(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(dummyTeam, false));
                }
            }
        } else {
            updateAll = true;
        }

        List<Packet<? super ClientGamePacketListener>> packets = new ArrayList<>();
        if (!updateAll) {
            packets.addAll(modificationPackets);
        } else {
            currentTeams.stream().map(ClientboundSetPlayerTeamPacket::createRemovePacket).forEach(packets::add);
            currentTeams.clear();
            int tabListOrder = sortedPlayers.size();
            for (ServerPlayer player : sortedPlayers) {
                DummyTeam dummyTeam = new DummyTeam(player, tabListOrder);
                packets.add(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(dummyTeam, true));
                currentTeams.add(dummyTeam);
                tabListOrder--;
            }
        }
        if (!packets.isEmpty()) {
            playerList.broadcastAll(new ClientboundBundlePacket(packets));
        }
    }

}