package org.litebridge.orm.api.merge;

import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.ast.WhenNotMatchedNode;

import java.util.function.Function;

public sealed class MergeWhenNotMatchedStep<MIS extends MergeInsertStep>
        extends MergeTerminal
        permits MergeWhenMatchedStep {

    protected final String targetTable;
    protected final LitebridgeContext litebridgeContext;

    public MergeWhenNotMatchedStep(final String targetTable,
                                   final QueryNode node,
                                   final LitebridgeContext litebridgeContext) {
        super(node);
        this.targetTable = targetTable;
        this.litebridgeContext = litebridgeContext;
    }

    public MergeWhenNotMatchedStep<MIS> whenNotMatched(final Function<MIS, MergeTerminal> insert) {
        final MIS mergeInsertStep = createMergeInsertStep();
        final MergeTerminal mergeTerminal = insert.apply(mergeInsertStep);
        final QueryNode terminalNode = MergeTerminalInspector.getNode(mergeTerminal);
        node = new WhenNotMatchedNode(node, null, terminalNode);
        return this;
    }

    @SuppressWarnings({"unchecked", "ConstantConditions"})
    private MIS createMergeInsertStep() {
        if (litebridgeContext.mode() == LitebridgeContext.Mode.DTO) {
            return (MIS) new DtoMergeInsertStep(targetTable, litebridgeContext);
        } else {
            return (MIS) new MergeInsertStep(targetTable, litebridgeContext);
        }
    }
}
