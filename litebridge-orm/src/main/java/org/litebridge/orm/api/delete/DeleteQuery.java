package org.litebridge.orm.api.delete;

import org.litebridge.orm.api.dto.delete.DtoDeleteWhereClause;
import org.litebridge.orm.api.sql.delete.SqlDeleteWhereClause;

/**
 * Marker interface for a delete query at various stages of construction.
 */
public sealed interface DeleteQuery permits DeleteTerminal, DeleteWhereConditionClauseTerminal, DtoDeleteWhereClause, SqlDeleteWhereClause {
}
