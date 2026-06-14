package org.litebridgedb.db.spi.query;

import org.litebridgedb.db.spi.Operation;

public interface SelectExpression {

    String toSql(final Operation operation);
}
