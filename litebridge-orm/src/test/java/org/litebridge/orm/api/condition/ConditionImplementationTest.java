package org.litebridge.orm.api.condition;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.ConditionGroupNode;
import org.litebridge.orm.engine.ast.ConditionNode;
import org.litebridge.orm.persistence.OrmTable;

import java.sql.Types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConditionImplementationTest {

    @Test
    @SuppressWarnings("unchecked")
    void testDtoConditionClauses() {
        final OrmTable ormTable = mock(OrmTable.class);
        final LitebridgeContext litebridgeContext = mock(LitebridgeContext.class);
        final Table table = new Table("TEST");
        final ColumnMetaData columnMetaData = new ColumnMetaData(table, "COL", true, Types.VARCHAR);
        when(ormTable.columnMetaDataForField("field")).thenReturn(columnMetaData);
        when(ormTable.columnMetaDataForField("otherField")).thenReturn(columnMetaData);

        final DtoConditionClauseStart<Object> start = new DtoConditionClauseStart<>(null, litebridgeContext);
        final CbDtoConditionClause<Object> clause = start.where("field");

        assertNotNull(clause);
        assertInstanceOf(CbDtoConditionClause.class, clause);

        final CbDtoConditionClauseTerminal<Object> terminal = clause.eq("val");
        assertNotNull(terminal);
        assertInstanceOf(ConditionNode.class, terminal.node());
        assertEquals(Operator.EQ, ((ConditionNode) terminal.node()).operator());
        assertEquals("val", ((ConditionNode) terminal.node()).rhs());

        final CbDtoConditionClause<Object> nextClause = terminal.and("otherField");
        assertNotNull(nextClause);
        assertEquals(LogicOperator.AND, ((ConditionNode) nextClause.eq("x").node()).logicOperator());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testSqlConditionClauses() {
        final String table = "TEST";
        final LitebridgeContext litebridgeContext = mock(LitebridgeContext.class);

        final SqlConditionClauseStart start = new SqlConditionClauseStart(table, null, litebridgeContext);
        final CbSqlConditionClause clause = start.where("COL");

        assertNotNull(clause);
        assertInstanceOf(CbSqlConditionClause.class, clause);

        final CbSqlConditionClauseTerminal terminal = clause.eq("val");
        assertNotNull(terminal);
        assertInstanceOf(ConditionNode.class, terminal.node());

        final CbSqlConditionClause nextClause = terminal.or("OTHER_COL");
        assertNotNull(nextClause);
        assertEquals(LogicOperator.OR, ((ConditionNode) nextClause.eq("x").node()).logicOperator());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testNestedConditions() {
        final OrmTable ormTable = mock(OrmTable.class);
        final LitebridgeContext litebridgeContext = mock(LitebridgeContext.class);
        final Table table = new Table("TEST");
        final ColumnMetaData columnMetaData = new ColumnMetaData(table, "COL", true, java.sql.Types.VARCHAR);
        when(ormTable.columnMetaDataForField("field")).thenReturn(columnMetaData);

        final DtoConditionClauseStart<Object> start = new DtoConditionClauseStart<>(null, litebridgeContext);
        final CbDtoConditionClause<Object> clause = start.where("field");
        final CbDtoConditionClauseTerminal<Object> terminal = clause.eq("val");

        final CbDtoConditionClauseTerminal<Object> nestedTerminal = terminal.and(q -> q.where("field").eq("innerVal"));

        assertInstanceOf(ConditionGroupNode.class, nestedTerminal.node());
        final ConditionGroupNode groupNode = (ConditionGroupNode) nestedTerminal.node();
        assertEquals(LogicOperator.AND, groupNode.logicOperator());
        assertInstanceOf(ConditionNode.class, groupNode.lastChild());
    }
}
