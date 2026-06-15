package org.litebridgedb.orm.function;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.tracking.FieldAccessor;

/**
 * Expression that selects a DTO field.
 * <p>
 * Instances of this class are initially created with only the {@code fieldName} value;
 * the formal field accessor and column are set later in the API chain.
 */
public final class SelectField implements Expression {

    private final String fieldName;
    private @Nullable FieldAccessor fieldAccessor;
    private @Nullable Column column;

    /**
     * Creates a new {@code SelectField} expression instance.
     *
     * @param fieldName The name of the DTO field to select.
     */
    public SelectField(String fieldName) {
        this.fieldName = fieldName;
    }

    public SelectField(final FieldAccessor fieldAccessor, final Column column) {
        this.fieldName = fieldAccessor.name();
        this.fieldAccessor = fieldAccessor;
        this.column = column;
    }

    /**
     * Returns the name of the DTO field to select.
     *
     * @return The name of the DTO field to select.
     */
    public String fieldName() {
        return fieldName;
    }

    /**
     * Gets the field accessor for the field.
     *
     * @return the field accessor for the field, or {@code null} if not set
     */
    public @Nullable FieldAccessor getFieldAccessor() {
        return fieldAccessor;
    }

    /**
     * Sets the field accessor for the field.
     *
     * @param fieldAccessor field accessor for the field
     */
    public void setFieldAccessor(final FieldAccessor fieldAccessor) {
        this.fieldAccessor = fieldAccessor;
    }

    /**
     * Gets the database column for this DTO field.
     *
     * @return the database column for this DTO field, or {@code null} if not set
     */
    public @Nullable Column getColumn() {
        return column;
    }

    /**
     * Sets the database column for this DTO field.
     *
     * @param column the database column for this DTO field
     */
    public void setColumn(final Column column) {
        this.column = column;
    }
}
