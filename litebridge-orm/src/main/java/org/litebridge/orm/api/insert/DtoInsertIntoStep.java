package org.litebridge.orm.api.insert;

import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.InsertNode;
import org.litebridge.orm.expression.ExpressionSpec;

/**
 * DTO-mode insert step for specifying columns to insert into.
 */
public final class DtoInsertIntoStep extends InsertIntoStep {

    private final Class<?> dtoClass;

    /**
     * Creates a new {@code DtoInsertIntoStep} instance.
     *
     * @param dtoClass          the mapped DTO/entity type to insert data into
     * @param litebridgeContext Litebridge context
     */
    public DtoInsertIntoStep(final Class<?> dtoClass,
                             final LitebridgeContext litebridgeContext) {
        super(litebridgeContext);
        this.dtoClass = dtoClass;
    }

    /**
     * Specifies the mapped DTO/entity fields to insert data into.
     *
     * @param fields the DTO/entity fields to insert
     * @return step to specify the values to insert
     */
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
