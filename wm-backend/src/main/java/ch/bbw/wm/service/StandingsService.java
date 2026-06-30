package ch.bbw.wm.service;

import ch.bbw.wm.dto.TableRow;
import ch.bbw.wm.entity.Match;
import ch.bbw.wm.entity.Team;
import ch.bbw.wm.repository.MatchRepository;
import ch.bbw.wm.repository.TeamRepository;
import ch.bbw.wm.service.strategy.TiebreakerStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Berechnet die Gruppentabelle.
 *
 * F4 (Sprint 1): Refactoring – sprechende Namen, kleine Methoden.
 * F1 (Sprint 2): Strategy-Pattern – die Sortierlogik ist austauschbar.
 *   Spring injiziert automatisch alle TiebreakerStrategy-Beans;
 *   der Service kennt keine konkrete Implementierung (Points/GoalDiff/FairPlay),
 *   nur das Interface.
 */
@Service
public class StandingsService {

    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;

    // Map von Strategie-Name → Strategie-Objekt, automatisch befüllt
    // mit allen Spring-Beans, die TiebreakerStrategy implementieren.
    private final Map<String, TiebreakerStrategy> strategies;

    @Autowired
    public StandingsService(
            TeamRepository teamRepository,
            MatchRepository matchRepository,
            List<TiebreakerStrategy> availableStrategies
    ) {
        this.teamRepository = teamRepository;
        this.matchRepository = matchRepository;
        this.strategies = availableStrategies.stream()
                .collect(Collectors.toMap(TiebreakerStrategy::getName, s -> s));
    }

    /**
     * Berechnet die Tabelle für eine Gruppe.
     *
     * @param group        Gruppenname, z.B. "A"
     * @param strategyName Name der Wertungslogik: "points" (default), "goalDifference" oder "fairPlay"
     */
    public List<TableRow> getTable(String group, String strategyName) {
        List<Team> teams = teamRepository.findByGroupIgnoreCase(group);
        List<TableRow> rows = initializeRows(teams);
        List<Match> matches = matchRepository.findByGroupIgnoreCase(group);

        for (Match match : matches) {
            if (match.isPlayed()) {
                applyMatchResult(rows, teams, match);
            }
        }

        TiebreakerStrategy strategy = resolveStrategy(strategyName);
        rows.sort(strategy.getComparator());
        return rows;
    }

    /**
     * Überladene Variante ohne Strategie-Parameter: nutzt "points" als Standard,
     * damit bestehender Aufrufcode (z.B. der Controller aus Sprint 1) weiterhin
     * kompiliert, ohne dass jede Aufrufstelle sofort angepasst werden muss.
     */
    public List<TableRow> getTable(String group) {
        return getTable(group, "points");
    }

    /**
     * Liefert alle verfügbaren Strategie-Namen, z.B. für ein Dropdown im Frontend.
     */
    public List<String> getAvailableStrategies() {
        return new ArrayList<>(strategies.keySet());
    }

    private TiebreakerStrategy resolveStrategy(String strategyName) {
        TiebreakerStrategy strategy = strategies.get(strategyName);
        if (strategy == null) {
            // Unbekannter Name → Fallback auf Punkte-Strategie statt Fehler zu werfen
            return strategies.get("points");
        }
        return strategy;
    }

    private List<TableRow> initializeRows(List<Team> teams) {
        List<TableRow> rows = new ArrayList<>();
        for (Team team : teams) {
            rows.add(new TableRow(team.getId(), team.getName(), 0, 0, 0, 0, 0, 0, 0));
        }
        return rows;
    }

    private void applyMatchResult(List<TableRow> rows, List<Team> teams, Match match) {
        int homeIndex = indexOfTeam(teams, match.getHomeTeamId());
        int awayIndex = indexOfTeam(teams, match.getAwayTeamId());

        if (homeIndex < 0 || awayIndex < 0) {
            return;
        }

        rows.set(homeIndex, addGoals(rows.get(homeIndex), match.getHomeGoals(), match.getAwayGoals()));
        rows.set(awayIndex, addGoals(rows.get(awayIndex), match.getAwayGoals(), match.getHomeGoals()));

        if (match.getHomeGoals() > match.getAwayGoals()) {
            rows.set(homeIndex, addWin(rows.get(homeIndex)));
            rows.set(awayIndex, addLoss(rows.get(awayIndex)));
        } else if (match.getHomeGoals() < match.getAwayGoals()) {
            rows.set(awayIndex, addWin(rows.get(awayIndex)));
            rows.set(homeIndex, addLoss(rows.get(homeIndex)));
        } else {
            rows.set(homeIndex, addDraw(rows.get(homeIndex)));
            rows.set(awayIndex, addDraw(rows.get(awayIndex)));
        }
    }

    private int indexOfTeam(List<Team> teams, Long teamId) {
        for (int i = 0; i < teams.size(); i++) {
            if (teams.get(i).getId().equals(teamId)) {
                return i;
            }
        }
        return -1;
    }

    private TableRow addGoals(TableRow row, int goalsFor, int goalsAgainst) {
        return new TableRow(
                row.teamId(), row.teamName(), row.played() + 1,
                row.won(), row.drawn(), row.lost(),
                row.goalsFor() + goalsFor, row.goalsAgainst() + goalsAgainst,
                row.points()
        );
    }

    private TableRow addWin(TableRow row) {
        return new TableRow(
                row.teamId(), row.teamName(), row.played(),
                row.won() + 1, row.drawn(), row.lost(),
                row.goalsFor(), row.goalsAgainst(), row.points() + 3
        );
    }

    private TableRow addLoss(TableRow row) {
        return new TableRow(
                row.teamId(), row.teamName(), row.played(),
                row.won(), row.drawn(), row.lost() + 1,
                row.goalsFor(), row.goalsAgainst(), row.points()
        );
    }

    private TableRow addDraw(TableRow row) {
        return new TableRow(
                row.teamId(), row.teamName(), row.played(),
                row.won(), row.drawn() + 1, row.lost(),
                row.goalsFor(), row.goalsAgainst(), row.points() + 1
        );
    }
}
