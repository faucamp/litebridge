package org.litebridge.orm.api.spec;

public sealed interface FieldColumnSpec
        permits FieldColumnSpecBuilder, FieldColumnSpecBuilder.EmbeddedColumnSpecBuilder {

    FieldSpec field();

    ColumnSpec column();
}
