package ch.bbw.wm.service;

import ch.bbw.wm.dto.TableRow;
import ch.bbw.wm.entity.Match;
import ch.bbw.wm.entity.Team;
import ch.bbw.wm.repository.MatchRepository;
import ch.bbw.wm.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Berechnet die Gruppentabelle.
 * F4: Refactoring – Code-Smell behoben:
 *   - Sprechende Variablennamen statt Einbuchstaber (ts → teams, ms → matches usw.)
 *   - Lange Methode aufgeteilt in calculateRow(), applyMatchResult(), sortTable()
 *   - Sortierung per Comparator statt handgeschriebenem Bubble-Sort
 *   - Keine Wiederholungen mehr (Tore-Logik in einer Hilfsmethode)
 */
@Service
public class StandingsService {

    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;

    @Autowired
    public StandingsService(TeamRepository teamRepository, MatchRepository matchRepository) {
        this.teamRepository = teamRepository;
        this.matchRepository = matchRepository;
    }

    public List<TableRow> getTable(String group) {
        List<Team> teams = teamRepository.findByGroupIgnoreCase(group);
        List<TableRow> rows = initializeRows(teams);
        List<Match> matches = matchRepository.findByGroupIgnoreCase(group);

        for (Match match : matches) {
            if (match.isPlayed()) {
                applyMatchResult(rows, teams, match);
            }
        }

        return sortByPoints(rows);
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

        TableRow homeRow = rows.get(homeIndex);
        TableRow awayRow = rows.get(awayIndex);

        rows.set(homeIndex, addGoals(homeRow, match.getHomeGoals(), match.getAwayGoals()));
        rows.set(awayIndex, addGoals(awayRow, match.getAwayGoals(), match.getHomeGoals()));

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
                row.teamId(), row.teamName(),
                row.played() + 1,
                row.won(), row.drawn(), row.lost(),
                row.goalsFor() + goalsFor,
                row.goalsAgainst() + goalsAgainst,
                row.points()
        );
    }

    private TableRow addWin(TableRow row) {
        return new TableRow(
                row.teamId(), row.teamName(), row.played(),
                row.won() + 1, row.drawn(), row.lost(),
                row.goalsFor(), row.goalsAgainst(),
                row.points() + 3
        );
    }

    private TableRow addLoss(TableRow row) {
        return new TableRow(
                row.teamId(), row.teamName(), row.played(),
                row.won(), row.drawn(), row.lost() + 1,
                row.goalsFor(), row.goalsAgainst(),
                row.points()
        );
    }

    private TableRow addDraw(TableRow row) {
        return new TableRow(
                row.teamId(), row.teamName(), row.played(),
                row.won(), row.drawn() + 1, row.lost(),
                row.goalsFor(), row.goalsAgainst(),
                row.points() + 1
        );
    }

    private List<TableRow> sortByPoints(List<TableRow> rows) {
        rows.sort(Comparator.comparingInt(TableRow::points).reversed());
        return rows;
    }
}
