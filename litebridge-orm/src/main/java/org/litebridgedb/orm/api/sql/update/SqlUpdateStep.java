package org.litebridgedb.orm.api.sql.update;

import org.litebridgedb.orm.api.update.UpdateQuery;

public sealed interface SqlUpdateStep extends SqlUpdateStart, UpdateQuery permits SqlUpdater {

    SqlUpdateWhereConditionClause where(final String column);

}
