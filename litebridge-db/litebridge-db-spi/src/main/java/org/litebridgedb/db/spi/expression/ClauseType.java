package org.litebridgedb.db.spi.expression;

public enum ClauseType {
    SELECT,
    JOIN,
    WHERE,
    GROUP_BY,
    HAVING,
    ORDER_BY;
}
