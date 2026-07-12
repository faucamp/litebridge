package org.litebridge.db.spi.update;

import org.litebridge.db.spi.ColumnMetaData;

import java.util.Collections;
import java.util.Map;
import java.util.StringJoiner;

/**
 * The result of an insert operation performed on the database.
 * <p>
 * This class extends {@link UpdateResult} and adds the capability to hold
 * any generated keys resulting from the insert operation.
 * <p>
 * Instances of this class encapsulate both the number of rows affected
 * by the insert operation and optionally a list of generated keys,
 * if applicable.
 */
public final class InsertResult extends UpdateResult {

    private final Map<ColumnMetaData, Object> generatedKeys;

    /**
     * Constructs an {@code InsertResult} instance representing the result of an insert
     * operation performed on the database.
     *
     * @param rowsAffected The number of rows affected by the insert operation.
     */
    public InsertResult(final int rowsAffected) {
        super(rowsAffected);
        this.generatedKeys = Collections.emptyMap();
    }

    /**
     * Constructs an {@code InsertResult} instance representing the result of an insert
     * operation performed on the database.
     *
     * @param rowsAffected  The number of rows affected by the insert operation.
     * @param generatedKeys A map of generated keys resulting from the insert operation.
     */
    public InsertResult(final int rowsAffected, final Map<ColumnMetaData, Object> generatedKeys) {
        super(rowsAffected);
        this.generatedKeys = generatedKeys;
    }

    /**
     * Returns a map of generated keys resulting from the insert operation.
     *
     * @return A map of generated keys.
     */
    public Map<ColumnMetaData, Object> generatedKeys() {
        return generatedKeys;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", InsertResult.class.getSimpleName() + "[", "]")
                .add("generatedKeys=" + generatedKeys)
                .toString();
    }
}
