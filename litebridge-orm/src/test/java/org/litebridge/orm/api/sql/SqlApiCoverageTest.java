//package org.litebridge.orm.api.sql;
//
//import org.junit.jupiter.api.Test;
//import org.litebridge.db.spi.Column;
//import org.litebridge.db.spi.Table;
//import org.litebridge.db.spi.query.LogicOperator;
//import org.litebridge.orm.api.select.ast.JoinNode;
//import org.litebridge.orm.api.select.ast.QueryNode;
//import org.litebridge.orm.api.select.ast.SelectNode;
//import org.litebridge.orm.engine.LitebridgeContext;
//import org.litebridge.orm.expression.ExpressionSpec;
//import org.litebridge.orm.expression.select.SelectColumnSpec;
//import org.litebridge.orm.persistence.TableRegistry;
//import org.litebridge.orm.persistence.TransactionalDatabaseProvider;
//
//import java.util.function.Function;
//
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.mockito.Mockito.mock;
//
//class SqlApiCoverageTest {
//
//    @Test
//    void testSqlApiClasses() {
//        final LitebridgeContext context = mock(LitebridgeContext.class);
//        final TransactionalDatabaseProvider databaseProvider = mock(TransactionalDatabaseProvider.class);
//        final QueryNode node = new SelectNode(null, new ExpressionSpec[0], null);
//        final Table table = new Table("TEST");
//        final Column column = new Column(table, "COL");
//        final ExpressionSpec expression = new SelectColumnSpec(column);
//
//        // SqlGroupByClauseTerminal
//        final TableRegistry tableRegistry = mock(TableRegistry.class);
//        final SqlSelector selector = new SqlSelector(table, databaseProvider, tableRegistry, context, node);
//        final SqlGroupByClauseTerminal groupByTerminal = new SqlGroupByClauseTerminal(selector);
//        assertNotNull(groupByTerminal.having(expression));
//        assertNotNull(groupByTerminal.orderBy("COL"));
//        assertNotNull(groupByTerminal.orderBy(expression));
//
//        // SqlHavingConditionClause
//        final Function<QueryNode, SqlHavingConditionClauseTerminal> recreator = n -> new SqlHavingConditionClauseTerminal(selector);
//        final SqlHavingConditionClause havingClause = new SqlHavingConditionClause(context, LogicOperator.AND, expression, node, recreator);
//        assertNotNull(havingClause);
//
//        // SqlHavingConditionClauseTerminal
//        final SqlHavingConditionClauseTerminal havingTerminal = new SqlHavingConditionClauseTerminal(selector);
//        assertNotNull(havingTerminal.and(expression));
//        assertNotNull(havingTerminal.or(expression));
//
//        // SqlOrderByClause
//        final SqlOrderByClause orderByClause = new SqlOrderByClause(new ExpressionSpec[]{expression}, selector);
//        assertNotNull(orderByClause.asc());
//        assertNotNull(orderByClause.desc());
//
//        // SqlOrderByClauseChain
//        final SqlOrderByClauseChain orderByChain = new SqlOrderByClauseChain(selector);
//        assertNotNull(orderByChain.then("COL"));
//
//        // SqlJoinClause
//        final JoinNode joinNode = new JoinNode(node, "INNER", null, "TEST");
//        final Function<QueryNode, SqlJoinConditionClauseTerminal> joinRecreator = n -> new SqlJoinConditionClauseTerminal(joinNode, selector);
//        final SqlJoinClause joinClause = new SqlJoinClause(selector, joinRecreator);
//        assertNotNull(joinClause.on("COL"));
//
//        // SqlJoinConditionClauseTerminal
//        final SqlJoinConditionClauseTerminal joinConditionTerminal = new SqlJoinConditionClauseTerminal(joinNode, selector);
//        assertNotNull(joinConditionTerminal);
//    }
//}
