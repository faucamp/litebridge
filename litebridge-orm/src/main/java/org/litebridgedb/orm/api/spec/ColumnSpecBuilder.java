package org.litebridgedb.orm.api.spec;

public sealed interface ColumnSpecBuilder<SELF extends AbstractColumnSpecBuilder<SELF>>
        extends ColumnMapping
        permits AbstractColumnSpecBuilder {

    ColumnSpecAutoIncrementTerminal<SELF> autoIncrement();

    SELF joinOn(String column);

    SELF joinUsing();
}
