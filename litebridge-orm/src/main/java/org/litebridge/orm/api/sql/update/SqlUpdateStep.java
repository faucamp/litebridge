package org.litebridge.orm.api.sql.update;

import org.litebridge.orm.api.update.UpdateQuery;

public sealed interface SqlUpdateStep extends SqlUpdateStart, UpdateQuery permits SqlUpdater {

    SqlUpdateWhereConditionClause where(final String column);

}
