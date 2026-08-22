package org.litebridge.orm.api.update;

import org.litebridge.orm.api.dto.update.DtoUpdateStep;
import org.litebridge.orm.api.sql.update.SqlUpdateStep;

public sealed interface UpdateQuery
        permits DtoUpdateStep, SqlUpdateStep, UpdateTerminal, UpdateWhereConditionClauseTerminal {
}
