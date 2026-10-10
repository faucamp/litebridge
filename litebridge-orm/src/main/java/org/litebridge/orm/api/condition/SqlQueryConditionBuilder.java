package org.litebridge.orm.api.condition;

/**
 * Functional interface for building SQL-mode query conditions.
 */
@FunctionalInterface
public interface SqlQueryConditionBuilder<ReturnType>
        extends QueryConditionBuilder<ReturnType,
        SqlConditionClauseStart<ReturnType>,
        CbSqlConditionClause<ReturnType>,
        CbSqlConditionClauseTerminal<ReturnType>,
        SqlQueryConditionBuilder<ReturnType>> {
}
