//package org.litebridge.orm.api.condition;
//
//import org.junit.jupiter.api.Test;
//import org.litebridge.db.spi.ColumnMetaData;
//import org.litebridge.db.spi.Table;
//import org.litebridge.db.spi.query.LogicOperator;
//import org.litebridge.db.spi.query.Operator;
//import org.litebridge.orm.api.dto.condition.CbDtoConditionClause;
//import org.litebridge.orm.api.dto.condition.DtoConditionClauseStart;
//import org.litebridge.orm.engine.ast.ConditionGroupNode;
//import org.litebridge.orm.engine.ast.ConditionNode;
//import org.litebridge.orm.api.sql.condition.CbSqlConditionClause;
//import org.litebridge.orm.api.sql.condition.SqlConditionClauseStart;
//import org.litebridge.orm.engine.FromClauseEngine;
//import org.litebridge.orm.persistence.OrmTable;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertInstanceOf;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//
//class ConditionImplementationTest {
//
//    @Test
//    @SuppressWarnings("unchecked")
//    void testDtoConditionClauses() {
//        final OrmTable ormTable = mock(OrmTable.class);
//        final FromClauseEngine engine = mock(FromClauseEngine.class);
//        final Table table = new Table("TEST");
//        final ColumnMetaData columnMetaData = new ColumnMetaData(table, "COL", true, java.sql.Types.VARCHAR);
//        when(ormTable.columnMetaDataForFieldName("field")).thenReturn(columnMetaData);
//        when(ormTable.columnMetaDataForFieldName("otherField")).thenReturn(columnMetaData);
//
//        final DtoConditionClauseStart<Object> start = new DtoConditionClauseStart<>(ormTable, engine, null);
//        final AbstractCbConditionClause<Object> clause = start.where("field");
//
//        assertNotNull(clause);
//        assertInstanceOf(CbDtoConditionClause.class, clause);
//
//        final AbstractCbConditionClauseTerminal<Object> terminal = (AbstractCbConditionClauseTerminal<Object>) clause.eq("val");
//        assertNotNull(terminal);
//        assertInstanceOf(ConditionNode.class, terminal.node());
//        assertEquals(Operator.EQ, ((ConditionNode) terminal.node()).operator());
//        assertEquals("val", ((ConditionNode) terminal.node()).rhs());
//
//        final AbstractCbConditionClause<Object> nextClause = terminal.and("otherField");
//        assertNotNull(nextClause);
//        assertEquals(LogicOperator.AND, ((ConditionNode) nextClause.eq("x").node()).logicOperator());
//    }
//
//    @Test
//    @SuppressWarnings("unchecked")
//    void testSqlConditionClauses() {
//        final Table table = new Table("TEST");
//        final FromClauseEngine engine = mock(FromClauseEngine.class);
//
//        final SqlConditionClauseStart start = new SqlConditionClauseStart(table, engine, null);
//        final AbstractCbConditionClause<org.litebridge.db.spi.Row> clause = start.where("COL");
//
//        assertNotNull(clause);
//        assertInstanceOf(CbSqlConditionClause.class, clause);
//
//        final AbstractCbConditionClauseTerminal<org.litebridge.db.spi.Row> terminal = (AbstractCbConditionClauseTerminal<org.litebridge.db.spi.Row>) clause.eq("val");
//        assertNotNull(terminal);
//        assertInstanceOf(ConditionNode.class, terminal.node());
//
//        final AbstractCbConditionClause<org.litebridge.db.spi.Row> nextClause = terminal.or("OTHER_COL");
//        assertNotNull(nextClause);
//        assertEquals(LogicOperator.OR, ((ConditionNode) nextClause.eq("x").node()).logicOperator());
//    }
//
//    @Test
//    @SuppressWarnings("unchecked")
//    void testNestedConditions() {
//        final OrmTable ormTable = mock(OrmTable.class);
//        final FromClauseEngine engine = mock(FromClauseEngine.class);
//        final Table table = new Table("TEST");
//        final ColumnMetaData columnMetaData = new ColumnMetaData(table, "COL", true, java.sql.Types.VARCHAR);
//        when(ormTable.columnMetaDataForFieldName("field")).thenReturn(columnMetaData);
//
//        final DtoConditionClauseStart<Object> start = new DtoConditionClauseStart<>(ormTable, engine, null);
//        final AbstractCbConditionClause<Object> clause = start.where("field");
//        final AbstractCbConditionClauseTerminal<Object> terminal = (AbstractCbConditionClauseTerminal<Object>) clause.eq("val");
//
//        final AbstractCbConditionClauseTerminal<Object> nestedTerminal = terminal.and(q -> q.where("field").eq("innerVal"));
//
//        assertInstanceOf(ConditionGroupNode.class, nestedTerminal.node());
//        final ConditionGroupNode groupNode = (ConditionGroupNode) nestedTerminal.node();
//        assertEquals(LogicOperator.AND, groupNode.logicOperator());
//        assertInstanceOf(ConditionNode.class, groupNode.lastChild());
//    }
//}
