package org.litebridge.orm.meta;

public final class QFInspector {

    private QFInspector() {
    }

    public static String getFieldName(final QueryField queryField) {
        return queryField.field();
    }

    public static Class<?> getDtoClass(final QueryField queryField) {
        return queryField.dtoClass();
    }
}
