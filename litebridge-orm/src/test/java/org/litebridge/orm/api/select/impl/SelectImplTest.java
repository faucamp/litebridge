package org.litebridge.orm.api.select.impl;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Table;
import org.litebridge.orm.api.select.LimitClauseTerminal;
import org.litebridge.orm.api.select.SelectTerminal;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.sql.SqlProtoExpressionResolver;
import org.litebridge.orm.api.sql.SqlSelectSpec;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.QueryPlanCache;
import org.litebridge.orm.persistence.TableRegistry;
import org.litebridge.orm.persistence.TransactionalDatabaseProvider;
import org.litebridge.orm.persistence.alias.NoOpAliasGenerator;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SelectImplTest {

    private SqlSelectSpec selectSpec;
    private TransactionalDatabaseProvider databaseProvider;
    private TestSelector selector;

    @BeforeEach
    void setUp() {
        selectSpec = new SqlSelectSpec(mock(LitebridgeContext.class));
        selectSpec.setProtoExpressionResolver(new SqlProtoExpressionResolver(selectSpec));
        selectSpec.setTable(new Table("CATALOG", "SCHEMA", "TABLE"));
        databaseProvider = mock(TransactionalDatabaseProvider.class);
        selector = new TestSelector(selectSpec, databaseProvider);
    }

    @Test
    void abstractSelector_firstOrThrow_supplier() {
        selector.setResult(null);
        assertThrows(RuntimeException.class, () -> selector.firstOrThrow(() -> new RuntimeException("test")));
    }

    @Test
    void delegatingSelector_methods() {
        DelegatingSelector<Object, SqlSelectSpec> delegating = new DelegatingSelector<>(selector);

        selector.setResult("one");
        assertEquals(Optional.of("one"), delegating.one());
        assertEquals("one", delegating.oneOrNull());
        assertEquals("one", delegating.oneOrThrow());
        assertEquals("one", delegating.oneOrThrow(() -> new RuntimeException()));

        assertEquals(Optional.of("one"), delegating.first());
        assertEquals("one", delegating.firstOrNull());
        assertEquals("one", delegating.firstOrThrow());
        assertEquals("one", delegating.firstOrThrow(() -> new RuntimeException()));

        selector.setResultList(List.of("one", "two"));
        assertEquals(2, delegating.stream().count());
        assertEquals(2, delegating.list().size());

        when(databaseProvider.toSql(any(), any())).thenReturn("SELECT 1");
        assertEquals("SELECT 1", delegating.toSql());
    }

    @Test
    void limitClauseTerminalImpl_offset() {
        LimitClauseTerminalImpl<Object, SqlSelectSpec> limitClause = new LimitClauseTerminalImpl<>(selector);
        SelectTerminal<Object> resultTerminal = limitClause.offset(10);

        // Use a real context for compilation
        final LitebridgeContext context = new LitebridgeContext(new org.litebridge.orm.config.LitebridgeConfig(), null, null, new QueryPlanCache(), new NoOpAliasGenerator());
        final TestSelector finalSelector = (TestSelector) resultTerminal;
        final TestSelector compiledSelector = new TestSelector(selectSpec, databaseProvider, context, finalSelector.node());

        compiledSelector.toSql(); // Trigger compilation
        assertEquals(10, selectSpec.getLimit().getOffset().get());
    }

    @Test
    void orderByClauseTerminalImpl_limit() {
        OrderByClauseTerminalImpl<Object, SqlSelectSpec> orderByClause = new OrderByClauseTerminalImpl<>(selector);
        LimitClauseTerminal<Object> resultTerminal = orderByClause.limit(20);

        // Use a real context for compilation
        final LitebridgeContext context = new LitebridgeContext(new org.litebridge.orm.config.LitebridgeConfig(), null, null, new QueryPlanCache(), new NoOpAliasGenerator());
        final TestSelector finalSelector = (TestSelector) ((DelegatingSelector) resultTerminal).delegate();
        final TestSelector compiledSelector = new TestSelector(selectSpec, databaseProvider, context, finalSelector.node());

        compiledSelector.toSql(); // Trigger compilation
        assertEquals(20, selectSpec.getLimit().getLimit().get());
    }

    private static class TestSelector extends AbstractSelector<Object, SqlSelectSpec> {
        private final SqlSelectSpec selectSpec;
        private Object result;
        private List<Object> resultList;

        protected TestSelector(SqlSelectSpec selectSpec, TransactionalDatabaseProvider databaseProvider, LitebridgeContext context, QueryNode node) {
            super(databaseProvider, mock(TableRegistry.class), Object.class, (LitebridgeContext<SqlSelectSpec>) context, node);
            this.selectSpec = selectSpec;
        }

        protected TestSelector(SqlSelectSpec selectSpec, TransactionalDatabaseProvider databaseProvider) {
            this(selectSpec, databaseProvider, mock(LitebridgeContext.class), null);
        }

        @Override
        public TestSelector withNode(QueryNode node) {
            return new TestSelector(selectSpec, databaseProvider, litebridgeContext, node);
        }

        @Override
        protected SqlSelectSpec createSelectSpec(org.litebridge.orm.persistence.alias.AliasGenerator aliasGenerator) {
            return selectSpec;
        }

        public void setResult(Object result) {
            this.result = result;
            this.resultList = result != null ? List.of(result) : List.of();
        }

        public void setResultList(List<Object> resultList) {
            this.resultList = resultList;
        }

        @Override
        public @Nullable Object oneOrNull() {
            return result;
        }

        @Override
        public @Nullable Object firstOrNull() {
            return result;
        }

        @Override
        public List<Object> list() {
            return resultList;
        }
    }
}
