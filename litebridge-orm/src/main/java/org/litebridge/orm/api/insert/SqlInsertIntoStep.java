package org.litebridge.orm.api.insert;

import org.jspecify.annotations.Nullable;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.InsertNode;
import org.litebridge.orm.expression.ExpressionSpec;

/**
 * SQL-mode insert step for specifying columns to insert into.
 */
public final class SqlInsertIntoStep extends InsertIntoStep {

    private final @Nullable String tableName;

    /**
     * Creates a new {@code SqlInsertIntoStep} instance.
     *
     * @param tableName         name of the table to insert data into
     * @param litebridgeContext the Litebridge context
     */
    public SqlInsertIntoStep(final String tableName,
                             final LitebridgeContext litebridgeContext) {
        super(litebridgeContext);
        this.tableName = tableName;
    }

    /**
     * Specifies the columns to insert data into.
     *
     * @param columns the column names to insert
     * @return step to specify the values to insert
     */
    public InsertValuesStep into(final String... columns) {
        final InsertNode insertNode = new InsertNode(tableName, null, columns);
        return new InsertValuesStep(insertNode, litebridgeContext);
    }

    @Override
    public InsertValuesStep into(final ExpressionSpec... expressions) {
        final InsertNode insertNode = new InsertNode(tableName, null, expressions);
        return new InsertValuesStep(insertNode, litebridgeContext);
    }
}
