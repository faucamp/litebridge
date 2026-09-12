package org.litebridge.orm.api.merge;

import org.litebridge.orm.engine.ast.InsertNode;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;

/**
 * Step for specifying values to insert in a {@code WHEN NOT MATCHED} clause.
 */
public class MergeInsertValuesStep {

    private final InsertNode insertNode;
    private final LitebridgeContext litebridgeContext;

    /**
     * Creates a new {@code MergeInsertValuesStep} instance.
     *
     * @param insertNode        the insert query node
     * @param litebridgeContext the Litebridge context
     */
    public MergeInsertValuesStep(final InsertNode insertNode, final LitebridgeContext litebridgeContext) {
        this.insertNode = insertNode;
        this.litebridgeContext = litebridgeContext;
    }

    /**
     * Specifies the values to insert using expression specifications.
     *
     * @param expression       the first expression
     * @param otherExpressions additional expressions
     * @return the merge terminal
     */
    public MergeTerminal values(final ExpressionSpec expression, final ExpressionSpec... otherExpressions) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Specifies the values to insert using literal values.
     *
     * @param values the values to insert
     * @return the merge terminal
     */
    public MergeTerminal values(final Object... values) {
        return new MergeTerminal(insertNode);
    }
}
