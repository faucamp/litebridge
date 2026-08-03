package org.litebridge.orm.api.sql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.expression.SqlFunctionRegistry;
import org.litebridge.orm.api.select.ast.SelectNode;
import org.litebridge.orm.config.LitebridgeConfig;
import org.litebridge.orm.engine.FromClauseEngine;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.QueryPlanCache;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.persistence.TableMetaDataCache;
import org.litebridge.orm.persistence.TableRegistry;
import org.litebridge.orm.persistence.TransactionalDatabaseProvider;
import org.litebridge.orm.persistence.alias.NoOpAliasGenerator;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.litebridge.orm.expression.Fn.ca;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class SqlSelectorTest {

    @Mock
    private TransactionalDatabaseProvider databaseProvider;

    @Mock
    private TableRegistry tableRegistry;

    private LitebridgeContext litebridgeContext;

    private SqlSelector sqlSelector;

    @BeforeEach
    void setUp() {
        final TableMetaDataCache tableMetaDataCache = new TableMetaDataCache(databaseProvider, databaseProvider.transactionManager());
        litebridgeContext = new LitebridgeContext(new LitebridgeConfig(), mock(FromClauseEngine.class), mock(SqlFunctionRegistry.class), new QueryPlanCache(), new NoOpAliasGenerator(), tableMetaDataCache);
        lenient().when(tableRegistry.getOrCreateSpiTable(any())).thenAnswer(invocation -> new Table((String) invocation.getArgument(0)));
        sqlSelector = new SqlSelector(new Table("dummy"), databaseProvider, tableRegistry, litebridgeContext, new SelectNode(null, new ExpressionSpec[0], null));
    }

    @Test
    void select_basic_columnNames() throws Exception {
        // When
        final SqlFromClause result = sqlSelector.select(new ExpressionSpec[0]);

        // Then
        assertNotNull(result);
    }

    @Test
    void select_basic_aliased() throws Exception {
        // When
        final SqlWhereConditionClauseTerminal result = sqlSelector.select(ca("COL1", "col1Alias"), ca("COL2", "col2Alias"))
                .from("TABLE")
                .where("col1Alias").eq(123);

        // Then
        assertNotNull(result);
    }
}
