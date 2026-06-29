package org.litebridgedb.orm.api.sql.update;

import org.litebridgedb.orm.expression.ColumnExpressionSpec;

public interface SqlUpdateStart {

    SqlUpdateSetStep set(final String column);

    SqlUpdateSetStep set(final ColumnExpressionSpec column);

}
