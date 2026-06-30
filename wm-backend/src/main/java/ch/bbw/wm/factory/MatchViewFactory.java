package ch.bbw.wm.factory;

import ch.bbw.wm.entity.Match;
import org.springframework.stereotype.Component;

@Component
public class MatchViewFactory {

    public MatchView createView(Match match) {
        if (match.isKnockout()) {
            return new KnockoutMatchView(match);
        }
        return new GroupMatchView(match);
    }
}
