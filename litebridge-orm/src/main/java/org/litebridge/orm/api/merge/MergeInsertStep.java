package org.litebridge.orm.api.merge;

import org.litebridge.orm.api.insert.InsertValuesStep;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.InsertNode;
import org.litebridge.orm.expression.ExpressionSpec;

/**
 * Step to specify the columns to insert into for a {@code WHEN NOT MATCHED INSERT} clause.
 */
public class MergeInsertStep {

    private final String table;
    private final LitebridgeContext litebridgeContext;

    /**
     * Creates a new {@code MergeInsertStep} instance.
     *
     * @param table             the table to insert into
     * @param litebridgeContext the Litebridge context
     */
    public MergeInsertStep(final String table, final LitebridgeContext litebridgeContext) {
        this.table = table;
        this.litebridgeContext = litebridgeContext;
    }

    /**
     * Creates a {@code WHEN NOT MATCHED INSERT} clause inserting values into columns
     * specified using type-safe expressions.
     *
     * @param expressions the expressions specifying columns to insert into
     * @return step to specify the values to insert
     */
    public InsertValuesStep insert(final ExpressionSpec... expressions) {
        final InsertNode insertNode = new InsertNode(table, null, expressions);
        return new InsertValuesStep(insertNode, litebridgeContext);
    }

    /**
     * Creates a {@code WHEN NOT MATCHED INSERT} clause inserting values into the specified columns.
     *
     * @param columns the columns to insert into
     * @return step to specify the values to insert
     */
    public InsertValuesStep insert(final String... columns) {
        final InsertNode insertNode = new InsertNode(table, null, columns);
        return new InsertValuesStep(insertNode, litebridgeContext);
    }
}
