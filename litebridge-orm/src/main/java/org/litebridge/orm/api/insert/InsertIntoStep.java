package org.litebridge.orm.api.insert;

import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;

public abstract sealed class InsertIntoStep permits DtoInsertIntoStep, SqlInsertIntoStep {

    protected final LitebridgeContext litebridgeContext;

    public InsertIntoStep(final LitebridgeContext litebridgeContext) {
        this.litebridgeContext = litebridgeContext;
    }

    public abstract InsertValuesStep into(final ExpressionSpec... expressions);
}
