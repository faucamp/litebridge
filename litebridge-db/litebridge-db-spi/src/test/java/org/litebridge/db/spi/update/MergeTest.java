package org.litebridge.db.spi.update;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.query.ConditionGroup;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MergeTest {

    @Test
    void constructor_validation_failsWhenBothNull() {
        // Given
        final Table table = new Table("T1");
        final Table using = new Table("T2");
        final ConditionGroup on = new ConditionGroup(Collections.emptyList());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> new Merge(table, using, on, null, null));
    }

    @Test
    void constructor_whenMatched() {
        // Given
        final Table table = new Table("T1");
        final Table using = new Table("T2");
        final ConditionGroup on = new ConditionGroup(Collections.emptyList());
        final Merge.WhenMatched<Merge.WhenMatchedOperation> matched =
                new Merge.WhenMatched<>(null, new Merge.MergeUpdate(List.of(new UpdateColumn("COL1"))));
        final List<Merge.WhenMatched<Merge.WhenMatchedOperation>> whenMatched = List.of(matched);

        // When
        final Merge merge = new Merge(table, using, on, whenMatched, null);

        // Then
        assertEquals(table, merge.table());
        assertEquals(using, merge.using());
        assertEquals(on, merge.on());
        assertEquals(whenMatched, merge.whenMatched());
    }

    @Test
    void constructor_whenNotMatched_andNestedRecords() {
        // Given
        final Table table = new Table("T1");
        final Table using = new Table("T2");
        final ConditionGroup on = new ConditionGroup(Collections.emptyList());
        final Merge.MergeInsert mergeInsert = new Merge.MergeInsert(List.of(new UpdateColumn("COL1")), 1);
        final Merge.WhenMatched<Merge.MergeInsert> notMatched = new Merge.WhenMatched<>(null, mergeInsert);
        final List<Merge.WhenMatched<Merge.MergeInsert>> whenNotMatched = List.of(notMatched);

        // When
        final Merge merge = new Merge(table, using, on, null, whenNotMatched);

        // Then
        assertEquals(whenNotMatched, merge.whenNotMatched());
        assertEquals(1, mergeInsert.rows());
        assertEquals(1, mergeInsert.columns().size());

        final Merge.MergeDelete mergeDelete1 = new Merge.MergeDelete();
        final Merge.MergeDelete mergeDelete2 = new Merge.MergeDelete();
        assertEquals(mergeDelete1, mergeDelete2);
        assertEquals(mergeDelete1.hashCode(), mergeDelete2.hashCode());
    }
}
