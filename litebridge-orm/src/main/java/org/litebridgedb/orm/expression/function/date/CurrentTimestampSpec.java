package org.litebridgedb.orm.expression.function.date;

import org.litebridgedb.orm.expression.TypeOverrideExpressionSpec;

import java.time.ZonedDateTime;

/**
 * {@code CURRENT_TIMESTAMP()}: Returns the current date/time from the database.
 */
public final class CurrentTimestampSpec implements TypeOverrideExpressionSpec<ZonedDateTime> {

    @Override
    public Class<ZonedDateTime> returnType() {
        return ZonedDateTime.class;
    }
}
