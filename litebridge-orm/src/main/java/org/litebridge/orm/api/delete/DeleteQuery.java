package org.litebridge.orm.api.delete;

import org.litebridge.orm.api.dto.delete.DtoDeleteWhereClause;
import org.litebridge.orm.api.dto.delete.DtoDeleteWhereConditionClauseTerminal;
import org.litebridge.orm.api.sql.delete.SqlDeleteWhereClause;

public sealed interface DeleteQuery permits DeleteTerminal, DeleteWhereConditionClauseTerminal, DtoDeleteWhereClause, SqlDeleteWhereClause {
}
