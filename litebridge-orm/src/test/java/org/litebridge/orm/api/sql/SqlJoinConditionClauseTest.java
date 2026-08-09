package org.litebridge.orm.api.sql;

import org.junit.jupiter.api.Test;
import org.litebridge.convert.DefaultTypeConverter;
import org.litebridge.db.spi.expression.SqlFunctionRegistry;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.select.model.SelectExpressionMapper;
import org.litebridge.orm.config.LitebridgeConfig;
import org.litebridge.orm.engine.FromClauseEngine;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.QueryPlanCache;
import org.litebridge.orm.persistence.TableMetaDataCache;
import org.litebridge.orm.persistence.alias.NoOpAliasGenerator;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class SqlJoinConditionClauseTest {

    @Test
    void constructor() {
        final LitebridgeContext context = new LitebridgeContext(new LitebridgeConfig(), mock(FromClauseEngine.class), mock(SqlFunctionRegistry.class), new QueryPlanCache(), new NoOpAliasGenerator(), mock(TableMetaDataCache.class), new DefaultTypeConverter(), mock(SelectExpressionMapper.class));
        // When
        final SqlJoinConditionClause result = new SqlJoinConditionClause(
                context,
                LogicOperator.AND,
                new org.litebridge.orm.expression.select.SelectColumnSpec(new org.litebridge.db.spi.Column(new org.litebridge.db.spi.Table("TEST"), "COL")),
                null,
                n -> mock(SqlJoinConditionClauseTerminal.class));

        // Then
        assertNotNull(result);
    }
}
