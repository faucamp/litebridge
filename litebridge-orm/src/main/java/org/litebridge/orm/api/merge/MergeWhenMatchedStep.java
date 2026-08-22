package org.litebridge.orm.api.merge;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Table;
import org.litebridge.orm.api.select.ast.DeleteNode;
import org.litebridge.orm.api.select.ast.MergeNode;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.ast.SetNode;
import org.litebridge.orm.api.select.ast.WhenMatchedNode;
import org.litebridge.orm.engine.LitebridgeContext;

import java.util.function.Function;

public sealed class MergeWhenMatchedStep<DTO, MUS extends MergeUpdateStep<DTO>>
        extends MergeWhenNotMatchedStep
        permits MergeOnConditionClauseTerminal {

    protected final MergeNode mergeNode;

    public MergeWhenMatchedStep(final MergeNode mergeNode,
                                final String usingTable,
                                final QueryNode node,
                                final LitebridgeContext litebridgeContext) {
        super(mergeNode.table(), usingTable, node, litebridgeContext);
        this.mergeNode = mergeNode;
    }

    public MergeWhenMatchedStep<DTO, MUS> whenMatched(
            final Function<MergeAndStep<DTO, MUS>, MergeWhenMatchedConditionClauseTerminal<DTO, MUS>> and,
            final Function<MUS, MergeTerminal> update) {
        final MergeAndStep<DTO, MUS> mergeAndStep = new MergeAndStep<>(targetTable, usingTable, null, litebridgeContext);
        final MergeWhenMatchedConditionClauseTerminal<DTO, MUS> mergeWhenMatchedConditionClauseTerminal = and.apply(mergeAndStep);
        final QueryNode andNode = mergeWhenMatchedConditionClauseTerminal.node();
        return whenMatched(update, andNode);
    }

    public MergeWhenMatchedStep<DTO, MUS> whenMatched(final Function<MUS, MergeTerminal> update) {
        return whenMatched(update, null);
    }

    public MergeWhenMatchedStep<DTO, MUS> whenMatched(final Function<MUS, MergeTerminal> update, final @Nullable QueryNode andNode) {
        final MUS mergeUpdateStep = createMergeUpdateStep();
        final MergeTerminal terminal = update.apply(mergeUpdateStep);
        final QueryNode terminalNode = terminal.node();
        final WhenMatchedNode whenMatchedNode;

        if (terminalNode instanceof SetNode setNode) {
            whenMatchedNode = new WhenMatchedNode(node, andNode, setNode, false);
        } else if (terminalNode instanceof DeleteNode deleteNode) {
            whenMatchedNode = new WhenMatchedNode(node, andNode, null, true);
        } else {
            throw new IllegalStateException("Unexpected terminal node type: " + terminalNode.getClass().getName());
        }

        return new MergeWhenMatchedStep<>(mergeNode, usingTable, whenMatchedNode, litebridgeContext);
    }

    protected MUS createMergeUpdateStep() {
        if (litebridgeContext.mode() == LitebridgeContext.Mode.DTO) {
            return (MUS) new DtoMergeUpdateStep<>(mergeNode.dtoClass(), node, litebridgeContext);
        } else {
            return (MUS) new SqlMergeUpdateStep(mergeNode.table(), node, litebridgeContext);
        }
    }
}
