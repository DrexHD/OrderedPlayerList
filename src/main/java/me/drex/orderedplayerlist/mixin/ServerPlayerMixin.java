package me.drex.orderedplayerlist.mixin;

import me.drex.orderedplayerlist.util.IServerPlayer;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin implements IServerPlayer {
    @Unique
    private int orderedPlayerList$tabListOrder;

    /**
     * @author Drex
     * @reason Fail-fast, this is the only thing this mod touches, and it **needs** to be applied.
     */
    @Overwrite
    public int getTabListOrder() {
        return orderedPlayerList$tabListOrder;
    }

    @Override
    public void orderedPlayerList$setTabListOrder(int tabListOrder) {
        orderedPlayerList$tabListOrder = tabListOrder;
    }
}
