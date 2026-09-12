package org.litebridge.orm.api.insert;

import org.jspecify.annotations.Nullable;
import org.litebridge.orm.api.merge.MergeTerminal;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.InsertValuesNode;
import org.litebridge.orm.engine.ast.QueryNode;

/**
 * Insert step for specifying the values to insert into a row.
 */
public final class InsertValuesStep extends MergeTerminal {

    private final LitebridgeContext litebridgeContext;

    /**
     * Creates a new {@code InsertValuesStep} instance.
     *
     * @param node              current query node
     * @param litebridgeContext Litebridge context
     */
    public InsertValuesStep(final QueryNode node,
                            final LitebridgeContext litebridgeContext) {
        super(node);
        this.litebridgeContext = litebridgeContext;
    }

    /**
     * Specifies a row of values to insert.
     *
     * @param values the values to insert
     * @return a new instance of {@code InsertValuesStep} allowing another row to be inserted
     */
    public InsertValuesStep values(final Object @Nullable ... values) {
        return new InsertValuesStep(new InsertValuesNode(node, values), litebridgeContext);
    }
}
