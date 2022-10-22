package me.drex.orderedplayerlist.mixin;

import net.minecraft.network.protocol.Packet;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerScoreboard.class)
public abstract class ServerScoreboardMixin {

    @Redirect(
            method = {"addPlayerToTeam", "removePlayerFromTeam", "onTeamAdded", "onTeamChanged", "onTeamRemoved"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V"
            )
    )
    public void ordered_playerlist$hideVanillaTeams(PlayerList list, Packet<?> packet) {
        // no-op
    }

}
