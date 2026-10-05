package org.litebridge.orm.api.update;

import org.litebridge.orm.api.delete.DtoDeleteStart;
import org.litebridge.orm.api.delete.SqlDeleteStart;
import org.litebridge.orm.engine.LitebridgeContext;

/**
 * Abstract base class for update and delete fluent API steps.
 */
public abstract sealed class UpdateStepBase
        permits DtoDeleteStart, SqlDeleteStart, DtoUpdateStart, DtoUpdateStep, SqlUpdateStart, SqlUpdateStep {

    /**
     * The Litebridge context.
     */
    protected final LitebridgeContext litebridgeContext;

    /**
     * Creates a new {@code UpdateStepBase} instance.
     *
     * @param litebridgeContext the Litebridge context
     */
    public UpdateStepBase(final LitebridgeContext litebridgeContext) {
        this.litebridgeContext = litebridgeContext;
    }
}
