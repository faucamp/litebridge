package org.litebridge.db.spi;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ForeignKeyConstraintTest {

    @Test
    void recordProperties() {
        // Given
        final Column fkCol = new Column(new Table("T2"), "ID");
        final ForeignKeyConstraint constraint = new ForeignKeyConstraint("FK_T1_T2", fkCol);

        // When & Then
        assertEquals("FK_T1_T2", constraint.name());
        assertEquals(fkCol, constraint.foreignKey());

        final ForeignKeyConstraint same = new ForeignKeyConstraint("FK_T1_T2", fkCol);
        assertEquals(constraint, same);
        assertEquals(constraint.hashCode(), same.hashCode());

        final ForeignKeyConstraint diff = new ForeignKeyConstraint("FK_DIFF", fkCol);
        assertNotEquals(constraint, diff);
    }
}
