package org.litebridge.orm.api.merge;

import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.MergeNode;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.ast.WhenNotMatchedNode;

import java.util.function.Function;

/**
 * Merge step for setting up {@code WHEN NOT MATCHED} clauses.
 *
 * @param <MIS> operation mode-specific {@code WHEN NOT MATCHED} insert step
 */
public sealed class MergeWhenNotMatchedStep<MIS extends MergeInsertStep>
        extends MergeTerminal
        permits MergeWhenMatchedStep {

    protected final MergeNode mergeNode;
    protected final LitebridgeContext litebridgeContext;

    /**
     * Creates a new {@code MergeWhenNotMatchedStep} instance.
     *
     * @param mergeNode         root merge node
     * @param node              current query node
     * @param litebridgeContext Litebridge context
     */
    public MergeWhenNotMatchedStep(final MergeNode mergeNode,
                                   final QueryNode node,
                                   final LitebridgeContext litebridgeContext) {
        super(node);
        this.mergeNode = mergeNode;
        this.litebridgeContext = litebridgeContext;
    }

    /**
     * Creates a {@code WHEN NOT MATCHED} clause.
     * <p>
     * The insert function specifies the target insert columns and the values to insert in the {@code WHEN NOT MATCHED THEN} fragment.
     *
     * @param insert a {@code Function} that accepts a {@code MergeInsertStep} implementation and produces a {@code MergeTerminal}.
     *               The function is used to define the data to insert.
     * @return this instance of {@code MergeWhenNotMatchedStep} for further chaining of merge match clauses.
     */
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
            return (MIS) new DtoMergeInsertStep(mergeNode.table(), litebridgeContext);
        } else {
            return (MIS) new MergeInsertStep(mergeNode.table(), litebridgeContext);
        }
    }
}
