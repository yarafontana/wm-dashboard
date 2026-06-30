package ch.bbw.wm.service.strategy;

import ch.bbw.wm.dto.TableRow;
import org.springframework.stereotype.Component;

import java.util.Comparator;

@Component
public class PointsStrategy implements TiebreakerStrategy {

    @Override
    public Comparator<TableRow> getComparator() {
        return Comparator
                .comparingInt(TableRow::points)
                .thenComparingInt(TableRow::goalDifference)
                .thenComparingInt(TableRow::goalsFor)
                .reversed();
    }

    @Override
    public String getName() {
        return "points";
    }
}
