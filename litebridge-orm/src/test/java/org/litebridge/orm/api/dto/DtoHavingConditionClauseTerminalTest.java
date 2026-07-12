package org.litebridge.orm.api.dto;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.orm.api.condition.QueryConditionBuilder;
import org.litebridge.orm.api.select.model.ConditionGroupSpec;
import org.litebridge.orm.api.select.model.ConditionSpec;
import org.litebridge.orm.api.select.model.OrderBySpec;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;
import org.litebridge.orm.persistence.OrmTable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DtoHavingConditionClauseTerminalTest {

    @Test
    void and_field() {
        // Given
        final DtoSelector<String> selector = mock(DtoSelector.class);
        final OrmTable ormTable = mock(OrmTable.class);
        final DtoSelectSpec selectSpec = mock(DtoSelectSpec.class);
        final ColumnMetaData columnMetaData = mock(ColumnMetaData.class);
        final Column column = mock(Column.class);

        when(selector.table()).thenReturn(ormTable);
        when(selector.selectSpec()).thenReturn(selectSpec);
        when(ormTable.getColumnForFieldName("field")).thenReturn(columnMetaData);
        when(columnMetaData.toColumn()).thenReturn(column);

        final ConditionGroupSpec groupSpec = mock(ConditionGroupSpec.class);
        when(selectSpec.currentHavingConditionGroupSpec()).thenReturn(groupSpec);
        when(groupSpec.newCondition(any(), any())).thenReturn(mock(ConditionSpec.class));
        when(selector.litebridgeContext()).thenReturn(mock(LitebridgeContext.class));

        final DtoHavingConditionClauseTerminal<String> terminal = new DtoHavingConditionClauseTerminal<>(selector);

        // When
        final DtoHavingConditionClause<String> result = terminal.and("field");

        // Then
        assertNotNull(result);
        verify(ormTable).getColumnForFieldName("field");
    }

    @Test
    void and_expression() {
        // Given
        final DtoSelector<String> selector = mock(DtoSelector.class);
        final DtoSelectSpec selectSpec = mock(DtoSelectSpec.class);
        final ExpressionSpec expression = new SelectColumnSpec(mock(Column.class));

        when(selector.selectSpec()).thenReturn(selectSpec);
        final ConditionGroupSpec groupSpec = mock(ConditionGroupSpec.class);
        when(selectSpec.currentHavingConditionGroupSpec()).thenReturn(groupSpec);
        when(groupSpec.newCondition(any(), any())).thenReturn(mock(ConditionSpec.class));
        when(selector.litebridgeContext()).thenReturn(mock(LitebridgeContext.class));

        final DtoHavingConditionClauseTerminal<String> terminal = new DtoHavingConditionClauseTerminal<>(selector);

        // When
        final DtoHavingConditionClause<String> result = terminal.and(expression);

        // Then
        assertNotNull(result);
    }

    @Test
    void and_query() {
        // Given
        final DtoSelector<String> selector = mock(DtoSelector.class);
        final DtoSelectSpec selectSpec = mock(DtoSelectSpec.class);
        final QueryConditionBuilder<String> query = mock(QueryConditionBuilder.class);
        final LitebridgeContext context = mock(LitebridgeContext.class);

        when(selector.selectSpec()).thenReturn(selectSpec);
        when(selector.litebridgeContext()).thenReturn(context);
        when(selectSpec.pushHavingConditionGroup(any())).thenReturn(mock(ConditionGroupSpec.class));

        final DtoHavingConditionClauseTerminal<String> terminal = new DtoHavingConditionClauseTerminal<>(selector);

        // When
        final DtoHavingConditionClauseTerminal<String> result = terminal.and(query);

        // Then
        assertNotNull(result);
        verify(selectSpec).pushHavingConditionGroup(any());
        verify(selectSpec).popHavingConditionGroup();
        verify(query).apply(any());
    }

    @Test
    void or_field() {
        // Given
        final DtoSelector<String> selector = mock(DtoSelector.class);
        final OrmTable ormTable = mock(OrmTable.class);
        final DtoSelectSpec selectSpec = mock(DtoSelectSpec.class);
        final ColumnMetaData columnMetaData = mock(ColumnMetaData.class);
        final Column column = mock(Column.class);

        when(selector.table()).thenReturn(ormTable);
        when(selector.selectSpec()).thenReturn(selectSpec);
        when(ormTable.getColumnForFieldName("field")).thenReturn(columnMetaData);
        when(columnMetaData.toColumn()).thenReturn(column);

        final ConditionGroupSpec groupSpec = mock(ConditionGroupSpec.class);
        when(selectSpec.currentHavingConditionGroupSpec()).thenReturn(groupSpec);
        when(groupSpec.newCondition(any(), any())).thenReturn(mock(ConditionSpec.class));
        when(selector.litebridgeContext()).thenReturn(mock(LitebridgeContext.class));

        final DtoHavingConditionClauseTerminal<String> terminal = new DtoHavingConditionClauseTerminal<>(selector);

        // When
        final DtoHavingConditionClause<String> result = terminal.or("field");

        // Then
        assertNotNull(result);
        verify(ormTable).getColumnForFieldName("field");
    }

    @Test
    void or_expression() {
        // Given
        final DtoSelector<String> selector = mock(DtoSelector.class);
        final DtoSelectSpec selectSpec = mock(DtoSelectSpec.class);
        final ExpressionSpec expression = new SelectColumnSpec(mock(Column.class));

        when(selector.selectSpec()).thenReturn(selectSpec);
        final ConditionGroupSpec groupSpec = mock(ConditionGroupSpec.class);
        when(selectSpec.currentHavingConditionGroupSpec()).thenReturn(groupSpec);
        when(groupSpec.newCondition(any(), any())).thenReturn(mock(ConditionSpec.class));
        when(selector.litebridgeContext()).thenReturn(mock(LitebridgeContext.class));

        final DtoHavingConditionClauseTerminal<String> terminal = new DtoHavingConditionClauseTerminal<>(selector);

        // When
        final DtoHavingConditionClause<String> result = terminal.or(expression);

        // Then
        assertNotNull(result);
    }

    @Test
    void or_query() {
        // Given
        final DtoSelector<String> selector = mock(DtoSelector.class);
        final DtoSelectSpec selectSpec = mock(DtoSelectSpec.class);
        final QueryConditionBuilder<String> query = mock(QueryConditionBuilder.class);
        final LitebridgeContext context = mock(LitebridgeContext.class);

        when(selector.selectSpec()).thenReturn(selectSpec);
        when(selector.litebridgeContext()).thenReturn(context);
        when(selectSpec.pushHavingConditionGroup(any())).thenReturn(mock(ConditionGroupSpec.class));

        final DtoHavingConditionClauseTerminal<String> terminal = new DtoHavingConditionClauseTerminal<>(selector);

        // When
        final DtoHavingConditionClauseTerminal<String> result = terminal.or(query);

        // Then
        assertNotNull(result);
        verify(selectSpec).pushHavingConditionGroup(any());
        verify(selectSpec).popHavingConditionGroup();
        verify(query).apply(any());
    }

    @Test
    void orderBy_fields() {
        // Given
        final DtoSelector<String> selector = mock(DtoSelector.class);
        final DtoSelectSpec selectSpec = mock(DtoSelectSpec.class);
        when(selector.selectSpec()).thenReturn(selectSpec);
        when(selectSpec.newOrderBy(any(List.class))).thenReturn(mock(OrderBySpec.class));

        final DtoHavingConditionClauseTerminal<String> terminal = new DtoHavingConditionClauseTerminal<>(selector);

        // When
        final DtoOrderByClause<String> result = terminal.orderBy("field1", "field2");

        // Then
        assertNotNull(result);
        verify(selectSpec).newOrderBy(any(List.class));
    }

    @Test
    void orderBy_expressions() {
        // Given
        final DtoSelector<String> selector = mock(DtoSelector.class);
        final DtoSelectSpec selectSpec = mock(DtoSelectSpec.class);
        when(selector.selectSpec()).thenReturn(selectSpec);
        when(selectSpec.newOrderBy(any(ExpressionSpec[].class))).thenReturn(mock(OrderBySpec.class));

        final DtoHavingConditionClauseTerminal<String> terminal = new DtoHavingConditionClauseTerminal<>(selector);

        // When
        final DtoOrderByClause<String> result = terminal.orderBy(new SelectColumnSpec(mock(Column.class)));

        // Then
        assertNotNull(result);
        verify(selectSpec).newOrderBy(any(ExpressionSpec[].class));
    }
}
