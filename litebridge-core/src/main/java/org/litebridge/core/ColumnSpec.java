package org.litebridge.core;

public final class ColumnSpec {

    private final String name;
    private final boolean autoIncrement;
    private final String sequence;

    public ColumnSpec(final String name, final boolean autoIncrement, final String sequence) {
        this.name = name;
        this.autoIncrement = autoIncrement;
        this.sequence = sequence;
    }

    public String getName() {
        return name;
    }

    public boolean isAutoIncrement() {
        return autoIncrement;
    }

    public String getSequence() {
        return sequence;
    }

    public static ColumnSpec c(final String columnName, final boolean autoIncrement, final String sequenceName) {
        return new ColumnSpec(columnName, autoIncrement, sequenceName);
    }

    public static ColumnSpec c(final String columnName, final boolean autoIncrement) {
        return new ColumnSpec(columnName, autoIncrement, null);
    }

    public static ColumnSpec c(final String columnName) {
        return c(columnName, false, null);
    }
}
