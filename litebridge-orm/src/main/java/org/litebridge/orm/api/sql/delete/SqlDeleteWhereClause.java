package org.litebridge.orm.api.sql.delete;

import org.litebridge.orm.api.delete.DeleteQuery;

public sealed interface SqlDeleteWhereClause extends DeleteQuery permits SqlDeletor {

    SqlDeleteWhereConditionClause where(final String column);
}
