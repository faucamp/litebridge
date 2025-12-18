package org.litebridge.db.api;

import java.util.Objects;
import java.util.StringJoiner;

public final class Column {

    private final String name;
    private final boolean nullable;
    private final int dataType;
    private final int size;
    private final int decimalDigits;
    private boolean autoIncrement;
    private String sequence;

    public Column(final String name, final boolean nullable, final int dataType, final int size, final int decimalDigits, final boolean autoIncrement, final String sequence) {
        this.name = name;
        this.nullable = nullable;
        this.dataType = dataType;
        this.size = size;
        this.decimalDigits = decimalDigits;
        this.autoIncrement = autoIncrement;
        this.sequence = sequence;
    }

    public Column(final String name, final boolean nullable, final int dataType, final int size) {
        this(name, nullable, dataType, size, 0, false, null);
    }

    public Column(final String name, final boolean nullable, final int dataType) {
        this(name, nullable, dataType, 0);
    }

    public String getName() {
        return name;
    }

    public boolean isNullable() {
        return nullable;
    }

    public int getDataType() {
        return dataType;
    }

    public int getSize() {
        return size;
    }

    public int getDecimalDigits() {
        return decimalDigits;
    }

    public boolean isAutoIncrement() {
        return autoIncrement;
    }

    public void setAutoIncrement(final boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(final String sequence) {
        this.sequence = sequence;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof final Column column)) return false;
        return nullable == column.nullable && dataType == column.dataType && size == column.size && decimalDigits == column.decimalDigits && autoIncrement == column.autoIncrement && Objects.equals(name, column.name) && Objects.equals(sequence, column.sequence);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, nullable, dataType, size, decimalDigits, autoIncrement, sequence);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Column.class.getSimpleName() + "[", "]")
                .add("name='" + name + "'")
                .add("nullable=" + nullable)
                .add("dataType=" + dataType)
                .add("size=" + size)
                .add("decimalDigits=" + decimalDigits)
                .add("autoIncrement=" + autoIncrement)
                .add("sequenceName='" + sequence + "'")
                .toString();
    }
}
