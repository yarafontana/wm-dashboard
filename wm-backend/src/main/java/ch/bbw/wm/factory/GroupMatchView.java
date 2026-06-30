package ch.bbw.wm.factory;

import ch.bbw.wm.entity.Match;

public class GroupMatchView implements MatchView {

    private final Match match;

    public GroupMatchView(Match match) {
        this.match = match;
    }

    @Override
    public Long getMatchId() {
        return match.getId();
    }

    @Override
    public String getDisplayType() {
        return "Gruppenspiel";
    }

    @Override
    public String getScoreSummary() {
        if (!match.isPlayed()) {
            return "noch nicht gespielt";
        }
        return match.getHomeGoals() + ":" + match.getAwayGoals();
    }

    @Override
    public Match getMatch() {
        return match;
    }
}
