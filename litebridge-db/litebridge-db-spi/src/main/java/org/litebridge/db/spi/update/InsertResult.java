package org.litebridge.db.spi.update;

import java.util.Collections;
import java.util.List;

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

    private final List<Object> generatedKeys;

    public InsertResult(final int rowsAffected) {
        super(rowsAffected);
        this.generatedKeys = Collections.emptyList();
    }

    public InsertResult(final int rowsAffected, final List<Object> generatedKeys) {
        super(rowsAffected);
        this.generatedKeys = generatedKeys;
    }

    public List<Object> generatedKeys() {
        return generatedKeys;
    }
}
