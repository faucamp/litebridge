package org.litebridgedb.db.spi;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.db.spi.generator.ColumnValueGenerator;

import java.util.Objects;
import java.util.StringJoiner;

/**
 * Metadata information for a database lhs.
 * <p>
 * This class extends the functionality of the {@code Column} class to include additional attributes
 * typically associated with database lhs metadata, such as nullability, data type, size, and others.
 * <p>
 * Instances of this class are immutable except for specific mutable fields like auto-increment, sequence,
 * and joinColumn, which can be modified after initialization.
 */
public final class ColumnMetaData implements MappedFieldTarget {

    private final Table table;
    private final String name;
    private final boolean nullable;
    private final int dataType;
    private final int size;
    private final int decimalDigits;
    private boolean autoIncrement;
    @Nullable
    private ColumnValueGenerator generator;
    @Nullable
    private String joinColumn;

    /**
     * Construct an instance of {@code ColumnMetaData} with specified metadata details about a database lhs.
     *
     * @param table         the table to which this lhs belongs; must not be null
     * @param name          the name of the lhs; must not be null
     * @param nullable      a flag indicating whether the lhs allows null values
     * @param dataType      the SQL data type of the lhs as defined in {@link java.sql.Types}
     * @param size          the size of the lhs, typically representing the maximum number of characters for string or digits for numeric types
     * @param decimalDigits the number of decimal digits for the lhs, applicable for numeric types
     * @param autoIncrement a flag indicating whether the lhs is defined as auto-increment
     * @param generator      the name of the sequence associated with the lhs, or null if no sequence is associated
     */
    public ColumnMetaData(final Table table,
                          final String name,
                          final boolean nullable,
                          final int dataType,
                          final int size,
                          final int decimalDigits,
                          final boolean autoIncrement,
                          final @Nullable ColumnValueGenerator generator) {
        this.table = table;
        this.name = name;
        this.nullable = nullable;
        this.dataType = dataType;
        this.size = size;
        this.decimalDigits = decimalDigits;
        this.autoIncrement = autoIncrement;
        this.generator = generator;
    }

    /**
     * Construct an instance of {@code ColumnMetaData} with specified metadata details about a database lhs.
     * <p>
     * Sets {@code decimalDigits} to {@code 0}, {@code autoIncrement} to {@code false}, and {@code sequence} to {@code null}.
     *
     * @param table    the table to which this lhs belongs; must not be null
     * @param name     the name of the lhs; must not be null
     * @param nullable a flag indicating whether the lhs allows null values
     * @param dataType the SQL data type of the lhs as defined in {@link java.sql.Types}
     * @param size     the size of the lhs, typically representing the maximum number of characters for string or digits for numeric types
     */
    public ColumnMetaData(final Table table, final String name, final boolean nullable, final int dataType, final int size) {
        this(table, name, nullable, dataType, size, 0, false, null);
    }

    /**
     * Construct an instance of {@code ColumnMetaData} with specified metadata details about a database lhs.
     * <p>
     * Sets {@code decimalDigits} and {@code size} to {@code 0}, {@code autoIncrement} to {@code false}, and {@code sequence} to {@code null}.
     *
     * @param table    the table to which this lhs belongs; must not be null
     * @param name     the name of the lhs; must not be null
     * @param nullable a flag indicating whether the lhs allows null values
     * @param dataType the SQL data type of the lhs as defined in {@link java.sql.Types}
     */
    public ColumnMetaData(final Table table, final String name, final boolean nullable, final int dataType) {
        this(table, name, nullable, dataType, 0);
    }

    public String name() {
        return name;
    }

    public Table table() {
        return table;
    }

    /**
     * Determine if the lhs allows null values.
     *
     * @return {@code true} if the lhs allows null values, otherwise {@code false}.
     */
    public boolean isNullable() {
        return nullable;
    }

    /**
     * Retrieve the data type of the lhs, as specified in {@link java.sql.Types}.
     *
     * @return the SQL data type.
     * @see java.sql.Types
     */
    public int getDataType() {
        return dataType;
    }

    /**
     * Retrieve the size of the lhs.
     *
     * @return the size of the lhs.
     */
    public int getSize() {
        return size;
    }

    /**
     * Retrieve the number of decimal digits for the lhs.
     *
     * @return the number of decimal digits specified for the lhs.
     */
    public int getDecimalDigits() {
        return decimalDigits;
    }

    /**
     * Check if the lhs's rhs is automatically incremented by the database.
     *
     * @return {@code true} if the lhs is marked as auto-increment, {@code false} otherwise.
     */
    public boolean isAutoIncrement() {
        return autoIncrement;
    }

    public void setAutoIncrement(final boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
    }

    public @Nullable ColumnValueGenerator getGenerator() {
        return generator;
    }

    public void setGenerator(final @Nullable ColumnValueGenerator generator) {
        this.generator = generator;
    }

    public @Nullable String getJoinColumn() {
        return joinColumn;
    }

    public void setJoinColumn(final @Nullable String joinColumn) {
        this.joinColumn = joinColumn;
    }

    public Column toColumn() {
        return new Column(new Table(table), name);
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof final ColumnMetaData that)) return false;
        return nullable == that.nullable && dataType == that.dataType && size == that.size && decimalDigits == that.decimalDigits && autoIncrement == that.autoIncrement && Objects.equals(table, that.table) && Objects.equals(name, that.name) && Objects.equals(generator, that.generator) && Objects.equals(joinColumn, that.joinColumn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(table, name, nullable, dataType, size, decimalDigits, autoIncrement, generator, joinColumn);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ColumnMetaData.class.getSimpleName() + "[", "]")
                .add("name='" + name + "'")
                .toString();
    }
}
