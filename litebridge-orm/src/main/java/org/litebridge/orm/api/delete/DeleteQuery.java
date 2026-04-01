package org.litebridge.orm.api.delete;

import org.litebridge.orm.api.dto.delete.DtoDeleteWhereClause;
import org.litebridge.orm.api.dto.delete.DtoDeleteWhereConditionClauseTerminal;

public sealed interface DeleteQuery permits DeleteTerminal, DeleteWhereConditionClauseTerminal, DtoDeleteWhereClause {
}
