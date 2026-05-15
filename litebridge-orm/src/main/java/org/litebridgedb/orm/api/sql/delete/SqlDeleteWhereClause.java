package org.litebridgedb.orm.api.sql.delete;

import org.litebridgedb.orm.api.delete.DeleteQuery;

public sealed interface SqlDeleteWhereClause extends DeleteQuery permits SqlDeletor {

    SqlDeleteWhereConditionClause where(final String column);
}
