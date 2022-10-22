package me.drex.orderedplayerlist.util;

import me.drex.orderedplayerlist.OrderedPlayerList;
import me.drex.orderedplayerlist.config.ConfigManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

import java.util.*;

public class OrderedPlayerListManager {

    public static final OrderedPlayerListManager MANAGER = new OrderedPlayerListManager();
    public static final boolean VANISH = FabricLoader.getInstance().isModLoaded("melius-vanish");
    private final List<PlayerListEntry> playerListEntries = new ArrayList<>();

    private OrderedPlayerListManager() {
    }

    public void init() {
        ServerTickEvents.START_SERVER_TICK.register(this::onTick);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> onJoin(handler.getPlayer()));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> onDisconnect(handler.getPlayer()));
        //if (VANISH) VanishEvents.VANISH_EVENT.register(this::onVanishEvent);
    }

    private void onTick(MinecraftServer server) {
        if (ConfigManager.INSTANCE.config.updateRate > 0 && server.getTickCount() % ConfigManager.INSTANCE.config.updateRate == 0) {
            updateChanged(server.getPlayerList());
        }
    }

    private synchronized void updateChanged(PlayerList playerList) {
        if (playerListEntries.size() <= 1) {
            for (PlayerListEntry playerListEntry : playerListEntries) {
                ServerPlayer player = playerList.getPlayer(playerListEntry.uuid());
                playerListEntry.modifyPacket(player).ifPresent(playerList::broadcastAll);
            }
        } else {
            Comparator<ServerPlayer> comparator = ConfigManager.INSTANCE.config.comparator;
            List<PlayerListEntry> shuffledEntries = new ArrayList<>(playerListEntries);
            // Checking order needs to be randomized, or we may run out of space between team ids during constant back and forth swapping
            Collections.shuffle(shuffledEntries);
            for (PlayerListEntry playerListEntry : shuffledEntries) {
                int index = playerListEntries.indexOf(playerListEntry);
                boolean incorrect;
                ServerPlayer player = playerList.getPlayer(playerListEntry.uuid());
                if (index == 0) {
                    // First entry is greater than the second
                    PlayerListEntry entry = playerListEntries.get(index + 1);
                    ServerPlayer other = playerList.getPlayer(entry.uuid());
                    incorrect = comparator.compare(player, other) > 0;
                } else {
                    // Entry is smaller than the previous entry
                    PlayerListEntry entry = playerListEntries.get(index - 1);
                    ServerPlayer other = playerList.getPlayer(entry.uuid());
                    incorrect = comparator.compare(player, other) < 0;
                }
                if (incorrect) {
                    // Incorrectly placed entry found, remove it and re-add it at it's correct location
                    playerList.broadcastAll(playerListEntry.removePacket());
                    playerListEntries.remove(index);
                    PlayerListEntry addEntry = addEntry(player);
                    playerList.broadcastAll(addEntry.addPacket());
                } else {
                    // Update all changed values
                    playerListEntry.modifyPacket(player).ifPresent(playerList::broadcastAll);
                }
            }
        }
    }

    private synchronized void onJoin(ServerPlayer player) {
        for (PlayerListEntry playerListEntry : playerListEntries) {
            player.connection.send(playerListEntry.addPacket());
        }
        PlayerListEntry playerListEntry = addEntry(player);
        player.getServer().getPlayerList().broadcastAll(playerListEntry.addPacket());
        // Joining player has not yet been added to the player list
        player.connection.send(playerListEntry.addPacket());
    }

    private synchronized void onDisconnect(ServerPlayer player) {
        Optional<PlayerListEntry> optional = getEntry(player);
        if (optional.isPresent()) {
            playerListEntries.remove(optional.get());
            player.getServer().getPlayerList().broadcastAll(optional.get().removePacket());
        } else {
            OrderedPlayerList.LOGGER.warn("Player {} left, but had no dummy team associated to them", player.getScoreboardName());
        }
    }

    // TODO: Uncomment this and only send join / leave packets if players can see the vanished player
    /*private void onVanishEvent(ServerPlayer player, boolean vanish) {
        Optional<PlayerListEntry> optional = getEntry(player);
        if (optional.isPresent()) {
            ClientboundSetPlayerTeamPacket packet = vanish ? optional.get().removePacket() : optional.get().addPacket();
            for (ServerPlayer serverPlayer : player.getServer().getPlayerList().getPlayers()) {
                if (!Permissions.check(serverPlayer, "vanish.feature.view") && !player.equals(serverPlayer)) {
                    serverPlayer.connection.send(packet);
                }
            }
        } else {
            OrderedPlayerList.LOGGER.warn("Player {} {}, but had no dummy team associated to them", player.getScoreboardName(), vanish ? "vanished" : "unvanished");
        }
    }*/

    private Optional<PlayerListEntry> getEntry(ServerPlayer player) {
        for (PlayerListEntry playerListEntry : playerListEntries) {
            if (playerListEntry.uuid().equals(player.getUUID())) {
                return Optional.of(playerListEntry);
            }
        }
        return Optional.empty();
    }

    private PlayerListEntry addEntry(ServerPlayer player) {
        // Special case
        if (playerListEntries.isEmpty()) {
            long weight = Long.MAX_VALUE / 2;
            PlayerListEntry playerListEntry = new PlayerListEntry(player.getUUID(), weight, new DummyTeam(player, weight));
            playerListEntries.add(playerListEntry);
            return playerListEntry;
        }

        Comparator<ServerPlayer> comparator = ConfigManager.INSTANCE.config.comparator;
        PlayerList playerList = player.getServer().getPlayerList();
        int index = 0;
        while (index < playerListEntries.size()) {
            PlayerListEntry playerListEntry = playerListEntries.get(index);
            ServerPlayer other = playerList.getPlayer(playerListEntry.uuid());
            if (comparator.compare(player, other) > 0) {
                index++;
            } else {
                break;
            }
        }
        // Calculate weights of previous and next entries
        long previousWeight = index == 0 ? 0 : playerListEntries.get(index - 1).weight();
        long nextWeight = index == playerListEntries.size() ? Long.MAX_VALUE : playerListEntries.get(index).weight();
        // Place the new weight in between
        long weight = (nextWeight / 2 + previousWeight / 2);
        PlayerListEntry playerListEntry = new PlayerListEntry(player.getUUID(), weight, new DummyTeam(player, weight));
        playerListEntries.add(index, playerListEntry);
        return playerListEntry;
    }

    record PlayerListEntry(UUID uuid, long weight, DummyTeam team) {

        ClientboundSetPlayerTeamPacket addPacket() {
            return ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team(), true);
        }

        ClientboundSetPlayerTeamPacket removePacket() {
            return ClientboundSetPlayerTeamPacket.createRemovePacket(team());
        }

        Optional<ClientboundSetPlayerTeamPacket> modifyPacket(ServerPlayer player) {
            return team().update(player) ? Optional.of(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team(), false)) : Optional.empty();
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof PlayerListEntry playerListEntry) {
                return this.uuid.equals(playerListEntry.uuid);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return uuid.hashCode();
        }
    }

}
