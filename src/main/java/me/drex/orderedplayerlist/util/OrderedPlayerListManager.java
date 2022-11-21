package me.drex.orderedplayerlist.util;

import me.drex.orderedplayerlist.OrderedPlayerList;
import me.drex.orderedplayerlist.config.Config;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class OrderedPlayerListManager {

    public static final OrderedPlayerListManager MANAGER = new OrderedPlayerListManager();
    private final List<PlayerListEntry> playerListEntries = new ArrayList<>();
    private final Random random = new Random();

    private OrderedPlayerListManager() {
    }

    public void init() {
        ServerTickEvents.START_SERVER_TICK.register(this::onTick);
    }

    private void onTick(MinecraftServer server) {
        if (Config.INSTANCE.updateRate > 0 && server.getTickCount() % Config.INSTANCE.updateRate == 0) {
            updateChanged(server.getPlayerList());
        }
    }

    private synchronized void updateChanged(PlayerList playerList) {
        if (playerListEntries.size() <= 1) {
            for (PlayerListEntry playerListEntry : playerListEntries) {
                playerListEntry.modifyPacket(playerListEntry.player(playerList)).ifPresent(playerList::broadcastAll);
            }
        } else {
            Comparator<ServerPlayer> comparator = Config.INSTANCE.order.comparator();
            // Checking order needs to be randomized, or we may run out of space between team ids during constant back and forth swapping
            int offset = random.nextInt(playerListEntries.size());
            for (int i = 0; i < playerListEntries.size(); i++) {
                int index = (i + offset) % playerListEntries.size();
                PlayerListEntry playerListEntry = playerListEntries.get(index);
                boolean incorrect;
                ServerPlayer player = playerListEntry.player(playerList);
                if (index == 0) {
                    // First entry is greater than the second
                    PlayerListEntry entry = playerListEntries.get(index + 1);
                    ServerPlayer other = entry.player(playerList);
                    incorrect = comparator.compare(player, other) > 0;
                } else {
                    // Entry is smaller than the previous entry
                    PlayerListEntry entry = playerListEntries.get(index - 1);
                    ServerPlayer other = entry.player(playerList);
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

    public synchronized void onJoin(ServerPlayer player) {
        PlayerList playerList = player.getServer().getPlayerList();
        for (PlayerListEntry playerListEntry : playerListEntries) {
            player.connection.send(playerListEntry.addPacket());
        }
        PlayerListEntry playerListEntry = addEntry(player);
        playerList.broadcastAll(playerListEntry.addPacket());
        // Joining player has not yet been added to the player list
        player.connection.send(playerListEntry.addPacket());
    }

    public synchronized void onDisconnect(ServerPlayer player) {
        Optional<PlayerListEntry> optional = getEntry(player);
        if (optional.isPresent()) {
            playerListEntries.remove(optional.get());
            player.getServer().getPlayerList().broadcastAll(optional.get().removePacket());
        } else {
            OrderedPlayerList.LOGGER.warn("Player {} left, but had no dummy team associated to them", player.getScoreboardName());
        }
    }

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

        Comparator<ServerPlayer> comparator = Config.INSTANCE.order.comparator();
        PlayerList playerList = player.getServer().getPlayerList();
        int index = 0;
        while (index < playerListEntries.size()) {
            PlayerListEntry playerListEntry = playerListEntries.get(index);
            if (comparator.compare(player, playerListEntry.player(playerList)) > 0) {
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

        @NotNull
        ServerPlayer player(PlayerList playerList) {
            return Objects.requireNonNull(playerList.getPlayer(uuid()), "Player List Entry is not present in PlayerList");
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
