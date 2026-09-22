package org.litebridge.orm.engine.compiler;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.query.Join;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.engine.ast.ConditionJoinUsingNode;
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
        final Table table = new Table("TEST_TABLE");
        final OrmTable ormTable = mock(OrmTable.class);
        final JoinNode joinNode = new JoinNode(null, Join.JoinType.INNER, Object.class, null, null, null, "testAlias");

        // When
        final JoinSpec joinSpec = new JoinSpec(joinNode);

        // Then
        assertEquals(joinNode, joinSpec.joinNode());
        assertNotNull(joinSpec.conditionGroupStack());
        assertNull(joinSpec.getConditionJoinUsingNode());

        // When / Then
        joinSpec.setConditionJoinUsingNode(new ConditionJoinUsingNode(null, LogicOperator.NOOP, null, null));
        assertNotNull(joinSpec.getConditionJoinUsingNode());
    }
}