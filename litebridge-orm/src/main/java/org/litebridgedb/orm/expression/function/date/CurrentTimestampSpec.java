package org.litebridgedb.orm.expression.function.date;

import org.litebridgedb.orm.expression.TypeOverrideExpression;

import java.time.ZonedDateTime;

/**
 * {@code CURRENT_TIMESTAMP()}: Returns the current date/time from the database.
 */
public final class CurrentTimestampSpec implements TypeOverrideExpression<ZonedDateTime> {

    @Override
    public Class<ZonedDateTime> returnType() {
        return ZonedDateTime.class;
    }
}
