package org.litebridge.orm.api.insert;

import org.litebridge.orm.api.select.ast.InsertNode;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;

public final class DtoInsertIntoStep extends InsertIntoStep {

    private final Class<?> dtoClass;

    public DtoInsertIntoStep(final Class<?> dtoClass,
                             final LitebridgeContext litebridgeContext) {
        super(litebridgeContext);
        this.dtoClass = dtoClass;
    }

    public InsertValuesStep into(final String... fields) {
        final InsertNode insertNode = new InsertNode(null, dtoClass, fields);
        return new InsertValuesStep(insertNode, litebridgeContext);
    }

    @Override
    public InsertValuesStep into(final ExpressionSpec... expressions) {
        final InsertNode insertNode = new InsertNode(null, dtoClass, expressions);
        return new InsertValuesStep(insertNode, litebridgeContext);
    }
}
