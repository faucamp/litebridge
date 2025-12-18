package org.litebridge.orm.api.spec;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.litebridge.commons.StringUtils;

/**
 * Specification of a database column, used to map DTO fields to target columns.
 * <p>
 * This class is immutable and provides various factory methods to create instances
 * with different configurations.
 */
@NullMarked
public final class ColumnSpec {

    /**
     * Database column name
     */
    private final String name;
    /**
     * Whether column is set to auto-increment
     */
    private final boolean autoIncrement;
    /**
     * Name of the sequence used to generate values for this column
     */
    @Nullable
    private final String sequence;

    /**
     * Constructs a new {@code ColumnSpec} with the provided parameters.
     *
     * @param name          the name of the column; must not be null or empty
     * @param autoIncrement indicates whether the column is auto-incrementing
     * @param sequence      the name of the sequence associated with the column, or null if no sequence is used
     */
    public ColumnSpec(final String name, final boolean autoIncrement, @Nullable final String sequence) {
        this.name = StringUtils.requireNonBlank(name, "Column name cannot be null");
        this.autoIncrement = autoIncrement;
        this.sequence = sequence;
    }

    /**
     * @return Database column name
     */
    public String getName() {
        return name;
    }

    /**
     * @return true if the column is set to auto-increment; false otherwise
     */
    public boolean isAutoIncrement() {
        return autoIncrement;
    }

    /**
     * Get the name of the sequence used to generate values for this column, if any.
     *
     * @return name of the sequence used to generate values for this column, or null if no sequence is used
     */
    public @Nullable String getSequence() {
        return sequence;
    }

    /**
     * Creates and returns a new {@code ColumnSpec} instance with the specified parameters.
     *
     * @param columnName    the name of the column; must not be null or empty
     * @param autoIncrement whether the column value is automatically incremented by the database
     * @param sequenceName  the name of the database sequence associated with the column, or null if no sequence is used
     * @return a new {@code ColumnSpec} object configured with the provided parameters
     */
    public static ColumnSpec c(final String columnName, final boolean autoIncrement, final String sequenceName) {
        return new ColumnSpec(columnName, autoIncrement, sequenceName);
    }

    /**
     * Creates and returns a new {@code ColumnSpec} instance with the specified parameters.
     * Equivalent to {@code c(columnName, autoIncrement, null)}.
     *
     * @param columnName    the name of the column; must not be null or empty
     * @param autoIncrement whether the column value is automatically incremented by the database
     * @return a new {@code ColumnSpec} object configured with the provided parameters
     */
    public static ColumnSpec c(final String columnName, final boolean autoIncrement) {
        return new ColumnSpec(columnName, autoIncrement, null);
    }

    /**
     * Creates and returns a new {@code ColumnSpec} instance with the specified parameters.
     * Equivalent to {@code c(columnName, false, null)}.
     *
     * @param columnName the name of the column; must not be null or empty
     * @return a new {@code ColumnSpec} object configured with the provided parameters
     */
    public static ColumnSpec c(final String columnName) {
        return c(columnName, false, null);
    }
}
