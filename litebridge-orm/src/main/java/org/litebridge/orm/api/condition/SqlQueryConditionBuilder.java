package org.litebridge.orm.api.condition;

import org.litebridge.db.spi.Row;

/**
 * Functional interface for building SQL-mode query conditions.
 */
@FunctionalInterface
public interface SqlQueryConditionBuilder extends QueryConditionBuilder<Row, SqlConditionClauseStart, CbSqlConditionClause, CbSqlConditionClauseTerminal, SqlQueryConditionBuilder> {
}
