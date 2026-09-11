package org.litebridge.db.spi.update;

import org.litebridge.db.spi.ColumnMetaData;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * The result of an insert operation performed on the database.
 * <p>
 * This class extends {@link UpdateOpResult} and adds the ability to hold
 * any generated keys resulting from the insert operation.
 * <p>
 * Instances of this class encapsulate both the number of rows affected
 * by the insert operation and optionally a map of generated keys,
 * if applicable.
 */
public final class InsertResult extends UpdateResult implements Result {

    /**
     * List of generated keys per row
     */
    private final List<Map<ColumnMetaData, Object>> generatedKeys;

    /**
     * Constructs an {@code InsertOpResult} instance representing the result of an insert
     * operation performed on the database.
     *
     * @param rowsInserted The number of rows inserted by the operation.
     */
    public InsertResult(final int rowsInserted) {
        super(rowsInserted);
        this.generatedKeys = Collections.emptyList();
    }

    /**
     * Constructs an {@code InsertOpResult} instance representing the result of a single insert
     * operation performed on the database.
     *
     * @param generatedKeys A map of generated keys resulting from the insert operation.
     */
    public InsertResult(final int rowsInserted, final Map<ColumnMetaData, Object> generatedKeys) {
        super(rowsInserted);
        this.generatedKeys = Collections.singletonList(generatedKeys);
    }

    /**
     * Constructs an {@code InsertOpResult} instance representing the result of a multi-row insert
     * operation performed on the database.
     *
     * @param rowsInserted  The number of rows inserted by the operation.
     * @param generatedKeys A map of generated keys resulting from the insert operation.
     */
    public InsertResult(final int rowsInserted, final List<Map<ColumnMetaData, Object>> generatedKeys) {
        super(rowsInserted);
        this.generatedKeys = generatedKeys;
    }

    /**
     * Returns a per-row list of generated keys resulting from the insert operation.
     *
     * @return List for each affected row, containing a map of generated keys.
     */
    public List<Map<ColumnMetaData, Object>> generatedKeys() {
        return generatedKeys;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", InsertResult.class.getSimpleName() + "[", "]")
                .add("generatedKeys=" + generatedKeys)
                .toString();
    }
}
