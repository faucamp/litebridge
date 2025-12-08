package org.litebridge.db.api.convert;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;

public class BooleanConverter {

    public Boolean convert(final Object value) {
        if (value == null) {
            return null;
        }

        return switch (value) {
            case Boolean bool -> bool;
            case Number number -> number.intValue() == 1;
            case String string -> Boolean.valueOf(string);
            default -> throw new IllegalArgumentException("Unsupported type: " + value.getClass().getName());
        };
    }

}
