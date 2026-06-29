package org.litebridgedb.orm.api.sql.delete;

import org.litebridgedb.orm.api.delete.DeleteQuery;
import org.litebridgedb.orm.expression.ColumnExpressionSpec;

public sealed interface SqlDeleteWhereClause extends DeleteQuery permits SqlDeletor {

    SqlDeleteWhereConditionClause where(final String column);

    SqlDeleteWhereConditionClause where(final ColumnExpressionSpec column);
}
