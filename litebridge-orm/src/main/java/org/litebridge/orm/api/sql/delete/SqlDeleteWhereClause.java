package org.litebridge.orm.api.sql.delete;

import org.litebridge.orm.api.delete.DeleteQuery;
import org.litebridge.orm.api.dto.delete.DtoDeleteWhereConditionClause;
import org.litebridge.orm.api.dto.delete.DtoDeletor;
import org.litebridge.orm.api.spec.FieldColumnSpec;

public sealed interface SqlDeleteWhereClause extends DeleteQuery permits SqlDeletor {

    SqlDeleteWhereConditionClause where(final String column);
}
