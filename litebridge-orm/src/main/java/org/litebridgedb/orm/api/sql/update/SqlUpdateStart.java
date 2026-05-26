package org.litebridgedb.orm.api.sql.update;

import org.litebridgedb.orm.api.spec.FieldColumnSpec;

public interface SqlUpdateStart {

    SqlUpdateSetStep set(final String column);

    SqlUpdateSetStep set(final FieldColumnSpec column);

}
