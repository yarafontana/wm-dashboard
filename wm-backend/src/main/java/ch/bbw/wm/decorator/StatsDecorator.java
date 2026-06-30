package ch.bbw.wm.decorator;

import ch.bbw.wm.entity.Match;
import ch.bbw.wm.factory.MatchView;

public class StatsDecorator extends MatchDecorator {

    public StatsDecorator(MatchView wrapped) {
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

        return base + String.format(" [Tore gesamt: %d, Differenz: %d]", totalGoals, goalDiff);
    }
}
