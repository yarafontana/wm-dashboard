package ch.bbw.wm.factory;

import ch.bbw.wm.entity.Match;
import com.fasterxml.jackson.annotation.JsonIgnore;

public interface MatchView {

    Long getMatchId();

    String getDisplayType();

    String getScoreSummary();

    @JsonIgnore
    Match getMatch();
}
