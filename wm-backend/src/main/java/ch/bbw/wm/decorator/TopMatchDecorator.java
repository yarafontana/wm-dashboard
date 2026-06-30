package ch.bbw.wm.decorator;

import ch.bbw.wm.entity.Match;
import ch.bbw.wm.factory.MatchView;

public class TopMatchDecorator extends MatchDecorator {

    private static final int TOP_MATCH_GOAL_THRESHOLD = 5;
    private static final int SURPRISE_GOAL_DIFFERENCE = 4;

    public TopMatchDecorator(MatchView wrapped) {
        super(wrapped);
    }

    @Override
    public String getScoreSummary() {
        String base = wrapped.getScoreSummary();
        Match match = wrapped.getMatch();

        if (!match.isPlayed()) {
            return base;
        }

        int totalGoals = match.getHomeGoals() + match.getAwayGoals();
        int goalDiff = Math.abs(match.getHomeGoals() - match.getAwayGoals());

        StringBuilder result = new StringBuilder(base);

        if (totalGoals >= TOP_MATCH_GOAL_THRESHOLD) {
            result.append(" ⭐ Top-Spiel");
        }

        if (goalDiff >= SURPRISE_GOAL_DIFFERENCE) {
            result.append(" ⚡ Überraschung");
        }

        return result.toString();
    }
}
