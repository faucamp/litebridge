package org.litebridge.orm.api.merge;

import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.InsertValuesNode;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.ast.WhenNotMatchedNode;

import java.util.function.Function;

public sealed class MergeWhenNotMatchedStep extends MergeTerminal permits MergeWhenMatchedStep {

    protected final String targetTable;
    protected final String usingTable;
    protected final LitebridgeContext litebridgeContext;

    public MergeWhenNotMatchedStep(final String targetTable,
                                   final String usingTable,
                                   final QueryNode node,
                                   final LitebridgeContext litebridgeContext) {
        super(node);
        this.targetTable = targetTable;
        this.usingTable = usingTable;
        this.litebridgeContext = litebridgeContext;
    }

    public MergeWhenNotMatchedStep whenNotMatched(final Function<MergeInsertStep, MergeTerminal> insert) {
        final MergeInsertStep mergeInsertStep = new MergeInsertStep(targetTable, litebridgeContext);
        final MergeTerminal mergeTerminal = insert.apply(mergeInsertStep);
        final QueryNode terminalNode = MergeTerminalInspector.getNode(mergeTerminal);
        return new MergeWhenNotMatchedStep(targetTable,
                usingTable,
                new WhenNotMatchedNode(node, null, terminalNode),
                litebridgeContext);
    }
}
