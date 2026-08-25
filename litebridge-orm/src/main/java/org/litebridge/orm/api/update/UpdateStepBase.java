package org.litebridge.orm.api.update;

import org.litebridge.orm.engine.LitebridgeContext;

public abstract class UpdateStepBase {

    protected final LitebridgeContext litebridgeContext;

    public UpdateStepBase(final LitebridgeContext litebridgeContext) {
        this.litebridgeContext = litebridgeContext;
    }
}
