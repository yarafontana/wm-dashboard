package ch.bbw.wm.service.strategy;

import ch.bbw.wm.dto.TableRow;

import java.util.Comparator;

public interface TiebreakerStrategy {

    Comparator<TableRow> getComparator();

    String getName();
}
