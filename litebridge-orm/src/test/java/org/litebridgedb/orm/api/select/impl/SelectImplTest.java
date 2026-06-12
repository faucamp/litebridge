package org.litebridgedb.orm.api.select.impl;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.orm.api.sql.SqlSelectSpec;
import org.litebridgedb.orm.config.LitebridgeConfig;
import org.litebridgedb.orm.persistence.TransactionalDatabaseProvider;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SelectImplTest {

    private SqlSelectSpec selectSpec;
    private TransactionalDatabaseProvider databaseProvider;
    private TestSelector selector;

    @BeforeEach
    void setUp() {
        selectSpec = new SqlSelectSpec();
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
        
        when(databaseProvider.toSql(any())).thenReturn("SELECT 1");
        assertEquals("SELECT 1", delegating.toSql());
    }

    @Test
    void limitClauseTerminalImpl_offset() {
        LimitClauseTerminalImpl<Object, SqlSelectSpec> limitClause = new LimitClauseTerminalImpl<>(selector);
        limitClause.offset(10);
        assertEquals(10, selectSpec.getLimit().getOffset().get());
    }

    @Test
    void orderByClauseTerminalImpl_limit() {
        OrderByClauseTerminalImpl<Object, SqlSelectSpec> orderByClause = new OrderByClauseTerminalImpl<>(selector);
        orderByClause.limit(20);
        assertEquals(20, selectSpec.getLimit().getLimit().get());
    }

    private static class TestSelector extends AbstractSelector<Object, SqlSelectSpec> {
        private Object result;
        private List<Object> resultList;

        protected TestSelector(SqlSelectSpec selectSpec, TransactionalDatabaseProvider databaseProvider) {
            super(selectSpec, databaseProvider, Object.class, new LitebridgeConfig());
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
