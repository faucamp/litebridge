package org.litebridge.db.spi;

import org.jspecify.annotations.Nullable;

import java.util.StringJoiner;

/**
 * Metadata information for a database column.
 * <p>
 * This class extends the functionality of the {@code Column} class to include additional attributes
 * typically associated with database column metadata, such as nullability, data type, size, and others.
 * <p>
 * Instances of this class are immutable except for specific mutable fields like auto-increment, sequence,
 * and joinColumn, which can be modified after initialization.
 */
public final class ColumnMetaData extends Column implements MappedFieldTarget {

    private final boolean nullable;
    private final int dataType;
    private final int size;
    private final int decimalDigits;
    private boolean autoIncrement;
    @Nullable
    private String sequence;
    @Nullable
    private String joinColumn;

    /**
     * Construct an instance of {@code ColumnMetaData} with specified metadata details about a database column.
     *
     * @param table         the table to which this column belongs; must not be null
     * @param name          the name of the column; must not be null
     * @param nullable      a flag indicating whether the column allows null values
     * @param dataType      the SQL data type of the column as defined in {@link java.sql.Types}
     * @param size          the size of the column, typically representing the maximum number of characters for string or digits for numeric types
     * @param decimalDigits the number of decimal digits for the column, applicable for numeric types
     * @param autoIncrement a flag indicating whether the column is defined as auto-increment
     * @param sequence      the name of the sequence associated with the column, or null if no sequence is associated
     */
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

    /**
     * Construct an instance of {@code ColumnMetaData} with specified metadata details about a database column.
     * <p>
     * Sets {@code decimalDigits} to {@code 0}, {@code autoIncrement} to {@code false}, and {@code sequence} to {@code null}.
     *
     * @param table    the table to which this column belongs; must not be null
     * @param name     the name of the column; must not be null
     * @param nullable a flag indicating whether the column allows null values
     * @param dataType the SQL data type of the column as defined in {@link java.sql.Types}
     * @param size     the size of the column, typically representing the maximum number of characters for string or digits for numeric types
     */
    public ColumnMetaData(final Table table, final String name, final boolean nullable, final int dataType, final int size) {
        this(table, name, nullable, dataType, size, 0, false, null);
    }

    /**
     * Construct an instance of {@code ColumnMetaData} with specified metadata details about a database column.
     * <p>
     * Sets {@code decimalDigits} and {@code size} to {@code 0}, {@code autoIncrement} to {@code false}, and {@code sequence} to {@code null}.
     *
     * @param table    the table to which this column belongs; must not be null
     * @param name     the name of the column; must not be null
     * @param nullable a flag indicating whether the column allows null values
     * @param dataType the SQL data type of the column as defined in {@link java.sql.Types}
     */
    public ColumnMetaData(final Table table, final String name, final boolean nullable, final int dataType) {
        this(table, name, nullable, dataType, 0);
    }

    /**
     * Copy constructor.
     * <p>
     * Creates a new instance of {@code ColumnMetaData} by copying the properties of another
     * {@code ColumnMetaData} object.
     * <p>
     * Creates a copy of the other column's {@link Table} instance,
     * so it can be safely aliased independently of the original column.
     *
     * @param other the {@code ColumnMetaData} object to copy; must not be null
     */
    public ColumnMetaData(final ColumnMetaData other, final String tableAlias) {
        super(new Table(other.table().as(tableAlias)), other.name(), tableAlias);
        this.nullable = other.nullable;
        this.dataType = other.dataType;
        this.size = other.size;
        this.decimalDigits = other.decimalDigits;
        this.autoIncrement = other.autoIncrement;
        this.sequence = other.sequence;
        this.joinColumn = other.joinColumn;
    }

    /**
     * Determine if the column allows null values.
     *
     * @return {@code true} if the column allows null values, otherwise {@code false}.
     */
    public boolean isNullable() {
        return nullable;
    }

    /**
     * Retrieve the data type of the column, as specified in {@link java.sql.Types}.
     *
     * @return the SQL data type.
     * @see java.sql.Types
     */
    public int getDataType() {
        return dataType;
    }

    /**
     * Retrieve the size of the column.
     *
     * @return the size of the column.
     */
    public int getSize() {
        return size;
    }

    /**
     * Retrieve the number of decimal digits for the column.
     *
     * @return the number of decimal digits specified for the column.
     */
    public int getDecimalDigits() {
        return decimalDigits;
    }

    /**
     * Check if the column's value is automatically incremented by the database.
     *
     * @return {@code true} if the column is marked as auto-increment, {@code false} otherwise.
     */
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

    /**
     * Create a copy of this {@code ColumnMetaData} object with a specified alias.
     *
     * @param alias the alias to be assigned; must not be null
     * @return a new {@code ColumnMetaData} instance with the specified alias
     */
    @Override
    public ColumnMetaData as(final String alias) {
        final ColumnMetaData copy = new ColumnMetaData(this, null);
        copy.setAlias(alias);
        return copy;
    }

    public ColumnMetaData as(final String alias, final String tableAlias) {
        final ColumnMetaData copy = new ColumnMetaData(this, tableAlias);
        copy.setAlias(alias);
        return copy;
    }

    @Override
    public boolean equals(final Object o) {
        return super.equals(o);
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
