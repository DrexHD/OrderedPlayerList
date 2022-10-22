package me.drex.orderedplayerlist.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.drex.orderedplayerlist.util.DummyTeam;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerTeam.class)
public abstract class PlayerTeamMixin {

    @WrapOperation(
            method = {"setDisplayName", "setPlayerPrefix", "setPlayerSuffix", "setAllowFriendlyFire", "setSeeFriendlyInvisibles", "setNameTagVisibility", "setDeathMessageVisibility", "setCollisionRule", "setColor"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/scores/Scoreboard;onTeamChanged(Lnet/minecraft/world/scores/PlayerTeam;)V"
            )
    )
    public void ordered_playerlist$preventDummyTeamScoreboard(Scoreboard scoreboard, PlayerTeam team, Operation<Void> original) {
        if (!((Object) this instanceof DummyTeam)) {
            original.call(scoreboard, team);
        }
    }


}
