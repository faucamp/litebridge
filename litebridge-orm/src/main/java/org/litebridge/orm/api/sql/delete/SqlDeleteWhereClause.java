package org.litebridge.orm.api.sql.delete;

import org.litebridge.orm.api.delete.DeleteQuery;
import org.litebridge.orm.expression.ExpressionSpec;

public sealed interface SqlDeleteWhereClause extends DeleteQuery permits SqlDeletor {

    SqlDeleteWhereConditionClause where(final String column);

    SqlDeleteWhereConditionClause where(final ExpressionSpec expression);
}
