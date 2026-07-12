package org.litebridge.orm.api.condition;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.orm.api.dto.condition.CbDtoConditionClause;
import org.litebridge.orm.api.dto.condition.CbDtoConditionClauseTerminal;
import org.litebridge.orm.api.dto.condition.DtoConditionClauseStart;
import org.litebridge.orm.api.select.model.ConditionGroupSpec;
import org.litebridge.orm.api.select.model.LogicConditionGroupSpec;
import org.litebridge.orm.api.select.model.ConditionSpec;
import org.litebridge.orm.api.sql.condition.CbSqlConditionClause;
import org.litebridge.orm.api.sql.condition.CbSqlConditionClauseTerminal;
import org.litebridge.orm.api.sql.condition.SqlConditionClauseStart;
import org.litebridge.orm.engine.FromClauseEngine;
import org.litebridge.orm.persistence.OrmTable;

import org.litebridge.orm.expression.select.SelectColumnSpec;
import org.litebridge.orm.expression.ExpressionSpec;

import java.sql.Types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConditionImplementationTest {

    @Test
    @SuppressWarnings("unchecked")
    void testDtoConditionClauses() {
        final ConditionGroupSpec group = mock(ConditionGroupSpec.class);
        when(group.newCondition(any(), any())).thenAnswer(invocation -> {
            ConditionSpec spec = new ConditionSpec();
            spec.setLhs((ExpressionSpec) invocation.getArgument(1));
            return spec;
        });

        final OrmTable ormTable = mock(OrmTable.class);
        final FromClauseEngine engine = mock(FromClauseEngine.class);
        final Table table = new Table("TEST");
        final ColumnMetaData columnMetaData = new ColumnMetaData(table, "COL", true, Types.VARCHAR);
        when(ormTable.getColumnForFieldName("field")).thenReturn(columnMetaData);
        when(ormTable.getColumnForFieldName("otherField")).thenReturn(columnMetaData);

        final DtoConditionClauseStart<Object> start = new DtoConditionClauseStart<>(group, ormTable, engine);
        final AbstractCbConditionClause<Object> clause = start.where("field");
        
        assertNotNull(clause);
        verify(group).newCondition(eq(LogicOperator.NOOP), any());

        final AbstractCbConditionClauseTerminal<Object> terminal = (AbstractCbConditionClauseTerminal<Object>) clause.eq("val");
        assertNotNull(terminal);

        final AbstractCbConditionClause<Object> nextClause = terminal.and("otherField");
        assertNotNull(nextClause);
        verify(ormTable).getColumnForFieldName("otherField");
        verify(group).newCondition(eq(LogicOperator.AND), any());

        final ExpressionSpec expr = new SelectColumnSpec(mock(org.litebridge.db.spi.Column.class));
        terminal.and(expr);
        verify(group).newCondition(eq(LogicOperator.AND), eq(expr));

        terminal.or(expr);
        verify(group).newCondition(eq(LogicOperator.OR), eq(expr));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testSqlConditionClauses() {
        final ConditionGroupSpec group = mock(ConditionGroupSpec.class);
        when(group.newCondition(any(), any())).thenAnswer(invocation -> {
            ConditionSpec spec = new ConditionSpec();
            spec.setLhs((ExpressionSpec) invocation.getArgument(1));
            return spec;
        });

        final Table table = new Table("TEST");
        final FromClauseEngine engine = mock(FromClauseEngine.class);

        final SqlConditionClauseStart start = new SqlConditionClauseStart(group, table, engine);
        final AbstractCbConditionClause<org.litebridge.db.spi.Row> clause = start.where("COL");
        
        assertNotNull(clause);
        verify(group).newCondition(eq(LogicOperator.NOOP), any());

        final AbstractCbConditionClauseTerminal<org.litebridge.db.spi.Row> terminal = (AbstractCbConditionClauseTerminal<org.litebridge.db.spi.Row>) clause.eq("val");
        assertNotNull(terminal);

        final AbstractCbConditionClause<org.litebridge.db.spi.Row> nextClause = terminal.or("OTHER_COL");
        assertNotNull(nextClause);
        verify(group).newCondition(eq(LogicOperator.OR), any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testNestedConditions() {
        final ConditionGroupSpec group = mock(ConditionGroupSpec.class);
        when(group.newCondition(any(), any())).thenAnswer(invocation -> {
            ConditionSpec spec = new ConditionSpec();
            spec.setLhs((ExpressionSpec) invocation.getArgument(1));
            return spec;
        });

        final OrmTable ormTable = mock(OrmTable.class);
        final FromClauseEngine engine = mock(FromClauseEngine.class);
        final Table table = new Table("TEST");
        final ColumnMetaData columnMetaData = new ColumnMetaData(table, "COL", true, Types.VARCHAR);
        when(ormTable.getColumnForFieldName("field")).thenReturn(columnMetaData);

        final DtoConditionClauseStart<Object> start = new DtoConditionClauseStart<>(group, ormTable, engine);
        final AbstractCbConditionClause<Object> clause = start.where("field");
        final AbstractCbConditionClauseTerminal<Object> terminal = (AbstractCbConditionClauseTerminal<Object>) clause.eq("val");

        final LogicConditionGroupSpec logicGroupSpec = mock(LogicConditionGroupSpec.class);
        final ConditionGroupSpec subgroup = mock(ConditionGroupSpec.class);
        when(subgroup.newCondition(any(), any())).thenAnswer(invocation -> {
            ConditionSpec spec = new ConditionSpec();
            spec.setLhs((ExpressionSpec) invocation.getArgument(1));
            return spec;
        });
        when(logicGroupSpec.conditionGroupSpec()).thenReturn(subgroup);
        when(group.newSubgroup(any())).thenReturn(logicGroupSpec);

        terminal.and(q -> q.where("field").eq("innerVal"));

        verify(group).newSubgroup(LogicOperator.AND);
        verify(subgroup).newCondition(eq(LogicOperator.NOOP), any());

        terminal.or(q -> q.where("field").eq("innerValOr"));
        verify(group).newSubgroup(LogicOperator.OR);
    }
}
