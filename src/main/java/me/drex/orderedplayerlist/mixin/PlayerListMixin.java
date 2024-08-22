package me.drex.orderedplayerlist.mixin;

import me.drex.orderedplayerlist.util.OrderedPlayerListManager;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {
    @Inject(
        method = "placeNewPlayer",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/protocol/game/ClientboundPlayerInfoUpdatePacket;createPlayerInitializing(Ljava/util/Collection;)Lnet/minecraft/network/protocol/game/ClientboundPlayerInfoUpdatePacket;",
            ordinal = 0
        )
    )
    public void orderedPlayerList_onPutPlayer(Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo ci) {
        OrderedPlayerListManager.updateAll((PlayerList) (Object) this, false);
    }
}
