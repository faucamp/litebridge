package org.litebridge.orm.api.merge;

import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.MergeNode;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.ast.WhenMatchedNode;

import java.util.function.Function;

/**
 * Merge step for setting up {@code WHEN MATCHED} and/or {@code WHEN NOT MATCHED} clauses.
 *
 * @param <DTO> the type of the DTO
 * @param <MUS> operation mode-specific {@code WHEN MATCHED} update step
 * @param <MIS> operation mode-specific {@code WHEN NOT MATCHED} insert step
 */
public sealed class MergeWhenMatchedStep<DTO, MUS extends MergeUpdateStep, MIS extends MergeInsertStep>
        extends MergeWhenNotMatchedStep<MIS>
        permits MergeOnConditionClauseTerminal {

    /**
     * Creates a new {@code MergeWhenMatchedStep} instance.
     *
     * @param mergeNode         root merge node
     * @param node              current query node
     * @param litebridgeContext Litebridge context
     */
    public MergeWhenMatchedStep(final MergeNode mergeNode,
                                final QueryNode node,
                                final LitebridgeContext litebridgeContext) {
        super(mergeNode, node, litebridgeContext);
    }

    /**
     * Creates a {@code WHEN MATCHED} clause.
     * <p>
     * A {@code WHEN MATCHED AND} conditional clause can be added by including
     * a {@code WHERE} clause in the specified update function.
     * <p>
     * The update function defines the {@code WHEN MATCHED THEN} behaviour to take when a match occurs in the {@code MERGE} operation.
     * This method allows specifying the action to perform, typically an update or delete,
     * through a provided function that operates on a {@code MergeUpdateStep}.
     * Any {@code WHERE} conditions added to the {@code MergeUpdateStep} will be applied
     * as part of the {@code WHEN MATCHED AND} clause.
     *
     * @param update a {@code Function} that accepts a {@code MergeUpdateStep} and produces a {@code MergeTerminal}.
     *               The function is used to define the specific update or delete operation to be executed for matched rows.
     * @return this instance of {@code MergeWhenMatchedStep} for further chaining of merge match clauses.
     */
    public MergeWhenMatchedStep<DTO, MUS, MIS> whenMatched(final Function<MUS, MergeTerminal> update) {
        final MUS mergeUpdateStep = createMergeUpdateStep();
        final MergeTerminal terminal = update.apply(mergeUpdateStep);
        node = new WhenMatchedNode(node, terminal.node());
        return this;
    }

    @SuppressWarnings({"unchecked", "ConstantConditions"})
    private MUS createMergeUpdateStep() {
        if (litebridgeContext.mode() == LitebridgeContext.Mode.DTO) {
            return (MUS) new DtoMergeUpdateStep<>(mergeNode.dtoClass(), node, litebridgeContext);
        } else {
            return (MUS) new SqlMergeUpdateStep(mergeNode.table(), node, litebridgeContext);
        }
    }
}
