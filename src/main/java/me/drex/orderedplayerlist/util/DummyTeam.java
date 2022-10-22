package me.drex.orderedplayerlist.util;

import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

/**
 * This is a dummy class for teams used in {@link OrderedPlayerListManager}
 * to allow for fake team packets, which don't interfere with
 * any scoreboard data. {@link me.drex.orderedplayerlist.mixin.PlayerTeamMixin}
 * is used to cancel any calls to {@link Scoreboard#onTeamChanged(PlayerTeam)},
 * which would otherwise throw a {@link NullPointerException}.
 */
public class DummyTeam extends PlayerTeam {

    public DummyTeam(String name) {
        super(null, name);
    }

    @Override
    public Scoreboard getScoreboard() {
        throw new UnsupportedOperationException();
    }

}
