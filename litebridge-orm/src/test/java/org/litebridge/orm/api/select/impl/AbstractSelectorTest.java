package org.litebridge.orm.api.select.impl;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.query.Select;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.ast.SelectNode;
import org.litebridge.orm.api.select.model.SelectSpec;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.QueryPlanCache;
import org.litebridge.orm.persistence.TableRegistry;
import org.litebridge.orm.persistence.TransactionalDatabaseProvider;
import org.litebridge.orm.persistence.alias.NoOpAliasGenerator;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AbstractSelectorTest {

    @Test
    void one() {
        // Given
        final SelectSpec selectSpec = mock(SelectSpec.class);
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final LitebridgeContext litebridgeContext = mock(LitebridgeContext.class);
        final QueryNode node = new SelectNode(null, new org.litebridge.orm.expression.ExpressionSpec[0], null);
        final TestSelector selector = new TestSelector(selectSpec, databaseProvider, String.class, litebridgeContext, node);
        selector.setResult(List.of("result"));

        // When
        final Optional<String> result = selector.one();

        // Then
        assertTrue(result.isPresent());
        assertEquals("result", result.get());
    }

    @Test
    void oneOrThrow() {
        // Given
        final SelectSpec selectSpec = mock(SelectSpec.class);
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final LitebridgeContext litebridgeContext = mock(LitebridgeContext.class);
        final QueryNode node = new SelectNode(null, new org.litebridge.orm.expression.ExpressionSpec[0], null);
        final TestSelector selector = new TestSelector(selectSpec, databaseProvider, String.class, litebridgeContext, node);
        selector.setResult(Collections.emptyList());

        // When / Then
        assertThrows(NoSuchElementException.class, selector::oneOrThrow);
    }

    @Test
    void first() {
        // Given
        final SelectSpec selectSpec = mock(SelectSpec.class);
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final LitebridgeContext litebridgeContext = mock(LitebridgeContext.class);
        final QueryNode node = new SelectNode(null, new org.litebridge.orm.expression.ExpressionSpec[0], null);
        final TestSelector selector = new TestSelector(selectSpec, databaseProvider, String.class, litebridgeContext, node);
        selector.setResult(List.of("result1", "result2"));

        // When
        final Optional<String> result = selector.first();

        // Then
        assertTrue(result.isPresent());
        assertEquals("result1", result.get());
    }

    @Test
    void toSql() {
        // Given
        final SelectSpec selectSpec = mock(SelectSpec.class);
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final LitebridgeContext litebridgeContext = mock(LitebridgeContext.class);
        final QueryNode node = new SelectNode(null, new org.litebridge.orm.expression.ExpressionSpec[0], null);
        final Select select = mock(Select.class);
        when(selectSpec.toSelect()).thenReturn(select);
        when(databaseProvider.toSql(any(), any())).thenReturn("SELECT * FROM TEST");
        final TestSelector selector = new TestSelector(selectSpec, databaseProvider, String.class, litebridgeContext, node);

        // When
        final String sql = selector.toSql();

        // Then
        assertEquals("SELECT * FROM TEST", sql);
    }

    @Test
    void executeQueryFailure() throws SQLException {
        // Given
        final SelectSpec selectSpec = mock(SelectSpec.class);
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final QueryPlanCache cache = mock(QueryPlanCache.class);
        final LitebridgeContext litebridgeContext = new LitebridgeContext(new org.litebridge.orm.config.LitebridgeConfig(), null, null, cache, new NoOpAliasGenerator());
        final QueryNode node = new SelectNode(null, new org.litebridge.orm.expression.ExpressionSpec[0], null);
        when(selectSpec.toSelect()).thenReturn(mock(Select.class));
        when(databaseProvider.prepareSql(any(), any())).thenReturn(new PreparedSql("SELECT 1", List.of()));
        when(databaseProvider.select(any(), any(), any())).thenThrow(new SQLException("DB Error"));
        final TestSelector selector = new TestSelector(selectSpec, databaseProvider, String.class, litebridgeContext, node);

        // When / Then
        assertThrows(IllegalStateException.class, selector::executeQuery);
    }

    @Test
    void selectSpecGetter() {
        // Given
        final SelectSpec selectSpec = mock(SelectSpec.class);
        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
        final LitebridgeContext litebridgeContext = mock(LitebridgeContext.class);
        final QueryNode node = new SelectNode(null, new org.litebridge.orm.expression.ExpressionSpec[0], null);
        final TestSelector selector = new TestSelector(selectSpec, databaseProvider, String.class, litebridgeContext, node);

        // When
        final SelectSpec result = selector.selectSpec();

        // Then
        assertSame(selectSpec, result);
    }

    private static class TestSelector extends AbstractSelector<String, SelectSpec> {
        private List<String> result = Collections.emptyList();
        private final SelectSpec selectSpec;

        protected TestSelector(SelectSpec selectSpec, TransactionalDatabaseProvider databaseProvider, Class<String> dtoClass, LitebridgeContext litebridgeContext, QueryNode node) {
            super(databaseProvider, mock(TableRegistry.class), dtoClass, litebridgeContext, node);
            this.selectSpec = selectSpec;
        }

        @Override
        public TestSelector withNode(QueryNode node) {
            return new TestSelector(selectSpec, databaseProvider, dtoClass, litebridgeContext, node);
        }

        public SelectSpec selectSpec() {
            return selectSpec;
        }

        @Override
        protected SelectSpec createSelectSpec(org.litebridge.orm.persistence.alias.AliasGenerator aliasGenerator) {
            return selectSpec;
        }

        public void setResult(List<String> result) {
            this.result = result;
        }

        @Override
        public @Nullable String oneOrNull() {
            return result.isEmpty() ? null : result.get(0);
        }

        @Override
        public @Nullable String firstOrNull() {
            return result.isEmpty() ? null : result.get(0);
        }

        @Override
        public List<String> list() {
            return result;
        }

        @Override
        protected List<Row> executeQuery() {
            return super.executeQuery();
        }
    }
}
