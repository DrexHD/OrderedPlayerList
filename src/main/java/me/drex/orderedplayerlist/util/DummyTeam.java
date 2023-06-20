package me.drex.orderedplayerlist.util;

import eu.pb4.placeholders.api.TextParserUtils;
import me.drex.orderedplayerlist.config.Config;
import me.lucko.fabric.api.permissions.v0.Options;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;

/**
 * This is a dummy class for teams used in {@link OrderedPlayerListManager}
 * to allow for fake team packets, which don't interfere with
 * any scoreboard data. {@link me.drex.orderedplayerlist.mixin.PlayerTeamMixin}
 * is used to cancel any calls to {@link Scoreboard#onTeamChanged(PlayerTeam)},
 * which would otherwise throw a {@link NullPointerException}.
 */
public class DummyTeam extends PlayerTeam {

    public DummyTeam(ServerPlayer player, long weight) {
        super(null, String.format("%019d", weight));
        getPlayers().add(player.getScoreboardName());
        update(player);
    }

    @Override
    public Scoreboard getScoreboard() {
        throw new UnsupportedOperationException();
    }

    public boolean update(ServerPlayer player) {
        boolean modified = false;
        ChatFormatting formatting = Options.get(player, "color", ChatFormatting.RESET, ChatFormatting::getByName);
        if (!formatting.equals(getColor())) {
            modified = true;
            setColor(formatting);
        }
        boolean collision = Options.get(player, "collision", true, Boolean::parseBoolean);
        Team.CollisionRule collisionRule = collision ? Team.CollisionRule.ALWAYS : Team.CollisionRule.NEVER;
        if (!collisionRule.equals(getCollisionRule())) {
            modified = true;
            setCollisionRule(collisionRule);
        }
        boolean nameTagVisible = Options.get(player, "nameTagVisible", true, Boolean::parseBoolean);
        Team.Visibility visibility = nameTagVisible ? Team.Visibility.ALWAYS : Team.Visibility.NEVER;
        if (!visibility.equals(getNameTagVisibility())) {
            modified = true;
            setNameTagVisibility(visibility);
        }
        if (Config.INSTANCE.displayPrefix) {
            Component prefix = Options.get(player, "prefix", Component.empty(), TextParserUtils::formatText);
            if (!prefix.equals(getPlayerPrefix())) {
                modified = true;
                setPlayerPrefix(prefix);
            }
        }
        if (Config.INSTANCE.displaySuffix) {
            Component suffix = Options.get(player, "suffix", Component.empty(), TextParserUtils::formatText);
            if (!suffix.equals(getPlayerSuffix())) {
                modified = true;
                setPlayerSuffix(suffix);
            }
        }
        return modified;
    }

}
