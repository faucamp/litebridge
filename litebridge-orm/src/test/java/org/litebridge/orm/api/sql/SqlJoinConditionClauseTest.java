//package org.litebridge.orm.api.sql;
//
//import org.junit.jupiter.api.Test;
//import org.litebridge.db.spi.DatabaseProvider;
//import org.litebridge.db.spi.query.LogicOperator;
//import org.litebridge.orm.config.LitebridgeConfig;
//import org.litebridge.orm.engine.FromClauseEngine;
//import org.litebridge.orm.engine.LitebridgeContext;
//import org.litebridge.orm.engine.QueryPlanCache;
//import org.litebridge.orm.persistence.TableMetaDataCache;
//import org.litebridge.orm.persistence.alias.NoOpAliasGenerator;
//
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.mockito.Mockito.mock;
//
//class SqlJoinConditionClauseTest {
//
//    @Test
//    void constructor() {
//        final LitebridgeContext context = new LitebridgeContext(LitebridgeContext.Mode.SQL, new LitebridgeConfig(), mock(DatabaseProvider.class), mock(FromClauseEngine.class), new QueryPlanCache(), new NoOpAliasGenerator(), mock(TableMetaDataCache.class));
//        // When
//        final SqlJoinConditionClause result = new SqlJoinConditionClause(
//                context,
//                LogicOperator.AND,
//                new org.litebridge.orm.expression.select.SelectColumnSpec(new org.litebridge.db.spi.Column(new org.litebridge.db.spi.Table("TEST"), "COL")),
//                null,
//                n -> mock(SqlJoinConditionClauseTerminal.class));
//
//        // Then
//        assertNotNull(result);
//    }
//}
