package ch.bbw.wm.decorator;

import ch.bbw.wm.entity.Match;
import ch.bbw.wm.factory.MatchView;

public abstract class MatchDecorator implements MatchView {

    protected final MatchView wrapped;

    protected MatchDecorator(MatchView wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public Long getMatchId() {
        return wrapped.getMatchId();
    }

    @Override
    public String getDisplayType() {
        return wrapped.getDisplayType();
    }

    @Override
    public String getScoreSummary() {
        return wrapped.getScoreSummary();
    }

    @Override
    public Match getMatch() {
        return wrapped.getMatch();
    }
}
