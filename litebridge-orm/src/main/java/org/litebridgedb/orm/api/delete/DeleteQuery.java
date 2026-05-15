package org.litebridgedb.orm.api.delete;

import org.litebridgedb.orm.api.dto.delete.DtoDeleteWhereClause;
import org.litebridgedb.orm.api.sql.delete.SqlDeleteWhereClause;

public sealed interface DeleteQuery permits DeleteTerminal, DeleteWhereConditionClauseTerminal, DtoDeleteWhereClause, SqlDeleteWhereClause {
}
