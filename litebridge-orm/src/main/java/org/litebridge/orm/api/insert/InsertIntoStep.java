package org.litebridge.orm.api.insert;

import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;

/**
 * Base class for {@code INSERT INTO} steps in the fluent insert API.
 */
public abstract sealed class InsertIntoStep permits DtoInsertIntoStep, SqlInsertIntoStep {

    protected final LitebridgeContext litebridgeContext;

    /**
     * Creates a new instance of {@link InsertIntoStep}.
     *
     * @param litebridgeContext Litebridge context
     */
    public InsertIntoStep(final LitebridgeContext litebridgeContext) {
        this.litebridgeContext = litebridgeContext;
    }

    /**
     * Specifies the columns to insert data into using expressions.
     *
     * @param expressions the expressions specifying columns to insert into
     * @return step to specify the values to insert
     */
    public abstract InsertValuesStep into(final ExpressionSpec... expressions);
}
