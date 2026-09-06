package org.litebridge.orm.engine.compiler;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Table;
import org.litebridge.orm.engine.ast.JoinNode;
import org.litebridge.orm.persistence.OrmTable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

class JoinSpecTest {

    @Test
    void gettersAndSetters() {
        // Given
        final Table table = new Table("TEST_TABLE").as("testAlias");
        final OrmTable ormTable = mock(OrmTable.class);
        final JoinNode joinNode = new JoinNode(null, "INNER", Object.class, null);

        // When
        final JoinSpec joinSpec = new JoinSpec(joinNode.type(), Object.class, null, ormTable, joinNode);

        // Then
        assertEquals(Object.class, joinSpec.dtoClass());
        assertNull(joinSpec.tableName());
        assertEquals(ormTable, joinSpec.ormTable());
        assertEquals(joinNode, joinSpec.joinNode());
        assertNotNull(joinSpec.conditionGroupStack());

        // When / Then
        joinSpec.setAliasedTable(table);
        assertEquals(table, joinSpec.getAliasedTable());
    }
}