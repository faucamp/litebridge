package org.litebridgedb.orm.api.sql.update;

import org.litebridgedb.orm.api.update.UpdateQuery;
import org.litebridgedb.orm.expression.ExpressionSpec;

public sealed interface SqlUpdateStep extends SqlUpdateStart, UpdateQuery permits SqlUpdater {

    SqlUpdateWhereConditionClause where(final String column);

    SqlUpdateWhereConditionClause where(final ExpressionSpec expression);

}
