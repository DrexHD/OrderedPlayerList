package me.drex.orderedplayerlist.mixin;

import me.drex.orderedplayerlist.util.OrderedPlayerListManager;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {

    @Redirect(
            method = "updateEntireScoreboard",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V",
                    ordinal = 0
            ),
            slice = @Slice(
                    to = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/server/ServerScoreboard;getDisplayObjective(Lnet/minecraft/world/scores/DisplaySlot;)Lnet/minecraft/world/scores/Objective;"
                    )
            )
    )
    public void orderedPlayerList_hideVanillaTeams(ServerGamePacketListenerImpl packetListener, Packet<?> packet) {
        // no-op
    }

    @Inject(
        method = "placeNewPlayer",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
            shift = At.Shift.AFTER
        )
    )
    public void orderedPlayerList_onPutPlayer(Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo ci) {
        OrderedPlayerListManager.MANAGER.onJoin(player);
    }

    @Inject(method = "remove", at = @At(value = "INVOKE", target = "Ljava/util/Map;remove(Ljava/lang/Object;)Ljava/lang/Object;", ordinal = 0))
    public void orderedPlayerList_onRemovePlayer(ServerPlayer player, CallbackInfo ci) {
        OrderedPlayerListManager.MANAGER.onDisconnect(player);
    }

}
