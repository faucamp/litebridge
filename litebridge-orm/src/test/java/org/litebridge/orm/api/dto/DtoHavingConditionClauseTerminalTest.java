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
import org.litebridge.orm.persistence.TableRegistry;
import org.litebridge.orm.persistence.TransactionalDatabaseProvider;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
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
        when(selector.withNode(any())).thenReturn(selector);

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
        when(selector.withNode(any())).thenReturn(selector);

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
        when(selector.withNode(any())).thenReturn(selector);

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
        when(selector.withNode(any())).thenReturn(selector);

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
        final DtoSelector<String> selector = createRealSelector(null);
        final DtoHavingConditionClauseTerminal<String> terminal = new DtoHavingConditionClauseTerminal<>(selector);

        final org.litebridge.orm.expression.select.SelectFieldSpec f1 = mock(org.litebridge.orm.expression.select.SelectFieldSpec.class);
        final org.litebridge.tracking.FieldAccessor fa1 = mock(org.litebridge.tracking.FieldAccessor.class);
        when(fa1.name()).thenReturn("field1");
        when(f1.field()).thenReturn(fa1);

        final org.litebridge.orm.expression.select.SelectFieldSpec f2 = mock(org.litebridge.orm.expression.select.SelectFieldSpec.class);
        final org.litebridge.tracking.FieldAccessor fa2 = mock(org.litebridge.tracking.FieldAccessor.class);
        when(fa2.name()).thenReturn("field2");
        when(f2.field()).thenReturn(fa2);

        // When
        final DtoOrderByClause<String> result = terminal.orderBy(f1, f2);
        final DtoOrderByClauseChain<String> chain = result.asc();

        // Then
        assertNotNull(chain);
        final org.litebridge.orm.api.select.ast.QueryNode lastNode = chain.delegate().node();
        assertNotNull(lastNode, "Last node should not be null");
        assertInstanceOf(org.litebridge.orm.api.select.ast.OrderByNode.class, lastNode);
        final org.litebridge.orm.api.select.ast.OrderByNode orderByNode2 = (org.litebridge.orm.api.select.ast.OrderByNode) lastNode;
        assertEquals("field2", ((org.litebridge.orm.expression.select.SelectFieldSpec) orderByNode2.expression()).field().name());

        final org.litebridge.orm.api.select.ast.QueryNode prevNode = orderByNode2.previous();
        assertNotNull(prevNode, "Previous node should not be null");
        assertInstanceOf(org.litebridge.orm.api.select.ast.OrderByNode.class, prevNode);
        final org.litebridge.orm.api.select.ast.OrderByNode orderByNode1 = (org.litebridge.orm.api.select.ast.OrderByNode) prevNode;
        assertEquals("field1", ((org.litebridge.orm.expression.select.SelectFieldSpec) orderByNode1.expression()).field().name());
    }

    @Test
    void orderBy_expressions() {
        // Given
        final DtoSelector<String> selector = createRealSelector(null);
        final DtoHavingConditionClauseTerminal<String> terminal = new DtoHavingConditionClauseTerminal<>(selector);

        // When
        final DtoOrderByClause<String> result = terminal.orderBy(new SelectColumnSpec(mock(org.litebridge.db.spi.Column.class)));
        final DtoOrderByClauseChain<String> chain = result.desc();

        // Then
        assertNotNull(chain);
        final org.litebridge.orm.api.select.ast.QueryNode lastNode = chain.delegate().node();
        assertNotNull(lastNode, "Last node should not be null");
        assertInstanceOf(org.litebridge.orm.api.select.ast.OrderByNode.class, lastNode);
        assertFalse(((org.litebridge.orm.api.select.ast.OrderByNode) lastNode).ascending());
    }

    private DtoSelector<String> createRealSelector(org.litebridge.orm.api.select.ast.QueryNode node) {
        final OrmTable ormTable = mock(OrmTable.class);
        final org.litebridge.db.spi.Table spiTable = new org.litebridge.db.spi.Table("TEST");
        final org.litebridge.db.spi.TableMetaData metaData = mock(org.litebridge.db.spi.TableMetaData.class);
        when(ormTable.getMetaData()).thenReturn(metaData);
        when(metaData.name()).thenReturn("TEST");
        when(metaData.toTable()).thenReturn(spiTable);
        when(ormTable.dtoClass()).thenReturn((Class) String.class);

        return new DtoSelector<>(
                String.class,
                ormTable,
                mock(TableRegistry.class),
                mock(org.litebridge.tracking.ClassFieldAccessorCache.class),
                mock(org.litebridge.orm.persistence.DtoConstructor.class),
                mock(TransactionalDatabaseProvider.class),
                new org.litebridge.orm.persistence.alias.NoOpAliasGenerator(),
                mock(LitebridgeContext.class),
                node
        );
    }
}
