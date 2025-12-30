package org.litebridge.orm.api.spec;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.BooleanUtils;

public final class FieldSpecBuilderImpl implements FieldSpecBuilder<FieldColumnSpecBuilder.EmbeddedColumnSpecBuilder> {

    /**
     * Built FieldSpec instance
     */
    @Nullable
    private FieldSpecImpl fieldSpec;

    /**
     * Field/property name
     */
    private final String name;
    /**
     * Whether the field is accessed as a property instead of direct field access
     */
    @Nullable
    private Boolean property;

    FieldSpecBuilderImpl(final String name) {
        this.name = name;
    }

    @Override
    public FieldSpecBuilderImpl property(final boolean property) {
        this.property = property;
        return this;
    }

    @Override
    public String name() {
        return ensureFieldSpec().name();
    }

    @Override
    public boolean property() {
        return ensureFieldSpec().property();
    }

    @Override
    public FieldColumnSpecBuilder.EmbeddedColumnSpecBuilder column(final String column) {
        return new FieldColumnSpecBuilder(this).column(column);
    }

    private FieldSpecImpl ensureFieldSpec() {
        if (fieldSpec == null) {
            fieldSpec = new FieldSpecImpl(name, BooleanUtils.toBoolean(property));
        }

        return fieldSpec;
    }
}
