package org.litebridge.db.spi;

import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.StringJoiner;

public final class ColumnMetaData extends Column {

    private final boolean nullable;
    private final int dataType;
    private final int size;
    private final int decimalDigits;
    private boolean autoIncrement;
    @Nullable
    private String sequence;
    @Nullable
    private String joinColumn;

    public ColumnMetaData(final Table table,
                          final String name,
                          final boolean nullable,
                          final int dataType,
                          final int size,
                          final int decimalDigits,
                          final boolean autoIncrement,
                          final @Nullable String sequence) {
        super(table, name);
        this.nullable = nullable;
        this.dataType = dataType;
        this.size = size;
        this.decimalDigits = decimalDigits;
        this.autoIncrement = autoIncrement;
        this.sequence = sequence;
    }

    public ColumnMetaData(final Table table, final String name, final boolean nullable, final int dataType, final int size) {
        this(table, name, nullable, dataType, size, 0, false, null);
    }

    public ColumnMetaData(final Table table, final String name, final boolean nullable, final int dataType) {
        this(table, name, nullable, dataType, 0);
    }

    public ColumnMetaData(final ColumnMetaData other) {
        super(other.table(), other.name(), other.alias());
        this.nullable = other.nullable;
        this.dataType = other.dataType;
        this.size = other.size;
        this.decimalDigits = other.decimalDigits;
        this.autoIncrement = other.autoIncrement;
        this.sequence = other.sequence;
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

    public @Nullable String getSequence() {
        return sequence;
    }

    public void setSequence(final @Nullable String sequence) {
        this.sequence = sequence;
    }

    public @Nullable String getJoinColumn() {
        return joinColumn;
    }

    public void setJoinColumn(final @Nullable String joinColumn) {
        this.joinColumn = joinColumn;
    }

    @Override
    public ColumnMetaData as(final String alias) {
        final ColumnMetaData copy = new ColumnMetaData(this);
        copy.setAlias(alias);
        return copy;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof final ColumnMetaData column)) return false;
        return nullable == column.nullable && dataType == column.dataType && size == column.size && decimalDigits == column.decimalDigits && autoIncrement == column.autoIncrement && Objects.equals(name(), column.name()) && Objects.equals(sequence, column.sequence);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name(), nullable, dataType, size, decimalDigits, autoIncrement, sequence);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ColumnMetaData.class.getSimpleName() + "[", "]")
                .add("name='" + name() + "'")
                .add("nullable=" + nullable)
                .add("dataType=" + dataType)
                .add("size=" + size)
                .add("decimalDigits=" + decimalDigits)
                .add("autoIncrement=" + autoIncrement)
                .add("sequenceName='" + sequence + "'")
                .toString();
    }
}
