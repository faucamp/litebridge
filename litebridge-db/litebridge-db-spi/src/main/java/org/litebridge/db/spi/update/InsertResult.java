package org.litebridge.db.spi.update;

import java.util.Collections;
import java.util.List;

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
