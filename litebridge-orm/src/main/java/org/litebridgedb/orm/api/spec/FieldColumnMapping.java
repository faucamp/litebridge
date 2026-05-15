package org.litebridgedb.orm.api.spec;

public final class FieldColumnMapping {

    private FieldColumnMapping() {
    }

    public static FieldColumnSpecBuilder f(final String field) {
        return field(field);
    }

    public static FieldColumnSpecBuilder field(final String field) {
        return new FieldColumnSpecBuilder(new FieldSpec(field, false));
    }

    public static FieldColumnSpecBuilder p(final String property) {
        return property(property);
    }

    public static FieldColumnSpecBuilder property(final String property) {
        return new FieldColumnSpecBuilder(new FieldSpec(property, true));
    }
}
