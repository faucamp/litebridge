package org.litebridgedb.db.spi.update;

import org.litebridgedb.db.spi.ColumnMetaData;

import java.util.Collections;
import java.util.Map;

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

    public InsertResult(final int rowsAffected) {
        super(rowsAffected);
        this.generatedKeys = Collections.emptyMap();
    }

    public InsertResult(final int rowsAffected, final Map<ColumnMetaData, Object> generatedKeys) {
        super(rowsAffected);
        this.generatedKeys = generatedKeys;
    }

    public Map<ColumnMetaData, Object> generatedKeys() {
        return generatedKeys;
    }
}
