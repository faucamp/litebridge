package org.litebridgedb.orm.api.spec;

public final class ColumnSpecAutoIncrementTerminal<CSB extends AbstractColumnSpecBuilder<CSB>> {

    private final CSB columnSpecBuilder;

    public ColumnSpecAutoIncrementTerminal(final CSB columnSpecBuilder) {
        this.columnSpecBuilder = columnSpecBuilder;
    }

    public CSB natively() {
        return columnSpecBuilder;
    }

    public CSB usingSequence(String sequence) {
        columnSpecBuilder.setSequence(sequence);
        return columnSpecBuilder;
    }
}
