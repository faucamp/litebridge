package org.litebridge.db.api.convert;

public class StringConverter {

    public String convert(final Object value) {
        if (value == null) {
            return null;
        }

        return value.toString();
    }
}
