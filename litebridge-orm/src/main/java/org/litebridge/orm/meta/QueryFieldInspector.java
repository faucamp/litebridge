package org.litebridge.orm.meta;

/**
 * Inspector for {@link QueryField} instances, providing access to their internal state.
 */
public final class QueryFieldInspector {

    private QueryFieldInspector() {
    }

    /**
     * Get the field name of the specified query field.
     *
     * @param queryField the query field to retrieve the field name for
     * @return the field name
     */
    public static String getFieldName(final QueryField queryField) {
        return queryField.field();
    }

    /**
     * Get the DTO class of the specified query field.
     *
     * @param queryField the query field to retrieve the DTO class for
     * @return the DTO class
     */
    public static Class<?> getDtoClass(final QueryField queryField) {
        return queryField.dtoClass();
    }
}
