package ch.bbw.wm.factory;

import ch.bbw.wm.entity.Match;

public class KnockoutMatchView implements MatchView {

    private final Match match;

    public KnockoutMatchView(Match match) {
        this.match = match;
    }

    @Override
    public Long getMatchId() {
        return match.getId();
    }

    @Override
    public String getDisplayType() {
        return Boolean.TRUE.equals(match.getExtraTime())
                ? "K.o.-Spiel (n. Verlängerung)"
                : "K.o.-Spiel";
    }

    @Override
    public String getScoreSummary() {
        if (!match.isPlayed()) {
            return "noch nicht gespielt";
        }

        String summary = match.getHomeGoals() + ":" + match.getAwayGoals();

        if (Boolean.TRUE.equals(match.getExtraTime())) {
            summary += " n.V.";
        }

        if (match.getPenaltyHome() != null && match.getPenaltyAway() != null) {
            summary += " (" + match.getPenaltyHome() + ":" + match.getPenaltyAway() + " i.E.)";
        }

        return summary;
    }

    @Override
    public Match getMatch() {
        return match;
    }
}
