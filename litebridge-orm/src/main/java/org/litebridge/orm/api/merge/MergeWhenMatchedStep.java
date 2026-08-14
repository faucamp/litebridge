package org.litebridge.orm.api.merge;

import org.litebridge.db.spi.Table;
import org.litebridge.orm.api.select.ast.DeleteNode;
import org.litebridge.orm.api.select.ast.MergeNode;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.ast.SetNode;
import org.litebridge.orm.api.select.ast.WhenMatchedNode;
import org.litebridge.orm.engine.LitebridgeContext;

import java.util.function.Function;

public class MergeWhenMatchedStep<DTO, MUS extends MergeUpdateStep<DTO>> {

    protected final Table table;
    protected final MergeNode mergeNode;
    protected final QueryNode node;
    protected final LitebridgeContext litebridgeContext;

    public MergeWhenMatchedStep(final Table table,
                                final MergeNode mergeNode,
                                final QueryNode node,
                                final LitebridgeContext litebridgeContext) {
        this.table = table;
        this.mergeNode = mergeNode;
        this.node = node;
        this.litebridgeContext = litebridgeContext;
    }

    public MergeWhenNotMatchedStep<DTO> whenMatched(final Function<MUS, MergeTerminal> update) {
        final MUS mergeUpdateStep = createMergeUpdateStep();
        final MergeTerminal terminal = update.apply(mergeUpdateStep);
        final QueryNode terminalNode = terminal.node();
        final WhenMatchedNode whenMatchedNode;

        if (terminalNode instanceof SetNode setNode) {
            whenMatchedNode = new WhenMatchedNode(terminalNode, setNode, null);
        } else if (terminalNode instanceof DeleteNode deleteNode) {
            whenMatchedNode = new WhenMatchedNode(terminalNode, null, deleteNode);
        } else {
            throw new IllegalStateException("Unexpected terminal node type: " + terminalNode.getClass().getName());
        }

        return new MergeWhenNotMatchedStep<>(mergeNode.table(), whenMatchedNode, litebridgeContext);
    }

    protected MUS createMergeUpdateStep() {
        if (litebridgeContext.mode() == LitebridgeContext.Mode.DTO) {
            return (MUS) new DtoMergeUpdateStep<>(mergeNode.dtoClass(), mergeNode.table(), node, litebridgeContext);
        } else {
            return (MUS) new SqlMergeUpdateStep(mergeNode.table(), node, litebridgeContext);
        }
    }
}
