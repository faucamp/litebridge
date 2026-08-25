package org.litebridge.orm.api.update;

import org.litebridge.orm.api.delete.DtoDeleteStart;
import org.litebridge.orm.api.delete.SqlDeleteStart;
import org.litebridge.orm.engine.LitebridgeContext;

public abstract sealed class UpdateStepBase
        permits DtoDeleteStart, SqlDeleteStart, DtoUpdateStart, DtoUpdateStep, SqlUpdateStart, SqlUpdateStep {

    protected final LitebridgeContext litebridgeContext;

    public UpdateStepBase(final LitebridgeContext litebridgeContext) {
        this.litebridgeContext = litebridgeContext;
    }
}
