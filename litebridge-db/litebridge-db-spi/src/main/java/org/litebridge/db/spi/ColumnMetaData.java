package org.litebridge.db.spi;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.generator.ColumnValueGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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
public final class ColumnMetaData implements MappedFieldTarget {

    private final Table table;
    private final String name;
    private final boolean nullable;
    private final int dataType;
    private final int size;
    private final int decimalDigits;
    private boolean autoIncrement;
    private @Nullable ColumnValueGenerator generator;
    private @Nullable String joinColumn;
    private @Nullable List<ForeignKeyConstraint> foreignKeyConstraints;
    private @Nullable List<ForeignKeyConstraint> foreignReferences;

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
     * @param generator     the name of the sequence associated with the column, or null if no sequence is associated
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
     * Gets the name of the column.
     *
     * @return the column name
     */
    public String name() {
        return name;
    }

    /**
     * Gets the table this column belongs to.
     *
     * @return the table
     */
    public Table table() {
        return table;
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

    /**
     * Sets whether this column is an auto-increment column.
     *
     * @param autoIncrement {@code true} if auto-increment; {@code false} otherwise
     */
    public void setAutoIncrement(final boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
    }

    /**
     * Gets the value generator for this column.
     *
     * @return the value generator, or {@code null} if none
     */
    public @Nullable ColumnValueGenerator getGenerator() {
        return generator;
    }

    /**
     * Sets the value generator for this column.
     *
     * @param generator the value generator to set
     */
    public void setGenerator(final @Nullable ColumnValueGenerator generator) {
        this.generator = generator;
    }

    /**
     * Gets the name of the join column, if applicable.
     *
     * @return the join column name
     */
    public @Nullable String getJoinColumn() {
        return joinColumn;
    }

    /**
     * Sets the name of the join column.
     *
     * @param joinColumn the join column name to set
     */
    public void setJoinColumn(final @Nullable String joinColumn) {
        this.joinColumn = joinColumn;
    }

    /**
     * Adds a foreign key constraint to this column.
     *
     * @param foreignKeyConstraint the constraint to add
     */
    public void addForeignKeyConstraint(final ForeignKeyConstraint foreignKeyConstraint) {
        if (foreignKeyConstraints == null) {
            foreignKeyConstraints = new ArrayList<>();
        }

        foreignKeyConstraints.add(foreignKeyConstraint);
    }

    /**
     * Gets all foreign key constraints associated with this column.
     *
     * @return the list of foreign key constraints
     */
    public List<ForeignKeyConstraint> getForeignKeyConstraints() {
        return foreignKeyConstraints != null ? foreignKeyConstraints : Collections.emptyList();
    }

    /**
     * Adds a foreign reference to this column.
     *
     * @param foreignKeyConstraint the reference to add
     */
    public void addForeignReference(final ForeignKeyConstraint foreignKeyConstraint) {
        if (foreignReferences == null) {
            foreignReferences = new ArrayList<>();
        }

        foreignReferences.add(foreignKeyConstraint);
    }

    /**
     * Gets all foreign references associated with this column.
     *
     * @return the list of foreign references
     */
    public List<ForeignKeyConstraint> getForeignReferences() {
        return foreignReferences != null ? foreignReferences : Collections.emptyList();
    }

    /**
     * Converts this metadata into a {@link Column} instance.
     *
     * @return a new {@link Column} instance
     */
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
