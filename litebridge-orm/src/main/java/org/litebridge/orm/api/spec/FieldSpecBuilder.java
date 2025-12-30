package org.litebridge.orm.api.spec;

public interface FieldSpecBuilder<CSB extends ColumnSpecBuilder<CSB>> extends FieldSpec {

    FieldSpecBuilder<CSB> property(boolean property);

    CSB column(String column);

    default CSB c(String column) {
        return column(column);
    }

    /**
     * Creates a new {@code FieldSpecBuilder} configured with the specified field/property name.
     *
     * @param field the name of the field/property in a class
     * @return this {@code FieldSpecBuilder} for further chaining
     */
    @SuppressWarnings("unchecked")
    static FieldSpecBuilder<FieldColumnSpecBuilder.EmbeddedColumnSpecBuilder> f(final String field) {
        return (FieldSpecBuilder<FieldColumnSpecBuilder.EmbeddedColumnSpecBuilder>) new FieldColumnSpecBuilder(field).field();
    }

    /**
     * Creates a new {@code FieldSpecBuilder} configured for property access of the specified property name.
     * Equivalent to {@code FieldSpecBuilder(property).property(true)}
     *
     * @param property the name of the property in a class
     * @return this {@code FieldSpecBuilder} for further chaining
     */
    static FieldSpecBuilder<FieldColumnSpecBuilder.EmbeddedColumnSpecBuilder> p(final String property) {
        return new FieldSpecBuilderImpl(property).property(true);
    }
}
