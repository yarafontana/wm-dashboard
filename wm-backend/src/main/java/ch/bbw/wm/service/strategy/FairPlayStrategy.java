package ch.bbw.wm.service.strategy;

import ch.bbw.wm.dto.TableRow;
import org.springframework.stereotype.Component;

import java.util.Comparator;

@Component
public class FairPlayStrategy implements TiebreakerStrategy {

    @Override
    public Comparator<TableRow> getComparator() {
        return Comparator
                .<TableRow>comparingInt(row -> fairPlayScore(row.teamId()))
                .thenComparingInt(TableRow::points)
                .reversed();
    }

    private int fairPlayScore(Long teamId) {

        return Math.floorMod(teamId * 31L, 100L) > 1 ? (int) Math.floorMod(teamId * 31L, 100L) : 1;
    }

    @Override
    public String getName() {
        return "fairPlay";
    }
}
