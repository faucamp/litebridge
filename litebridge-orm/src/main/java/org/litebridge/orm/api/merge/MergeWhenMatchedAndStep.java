package org.litebridge.orm.api.merge;

import org.litebridge.db.spi.Table;
import org.litebridge.orm.api.insert.InsertValuesStep;
import org.litebridge.orm.api.insert.InsertValuesStepInspector;
import org.litebridge.orm.api.select.ast.InsertValuesNode;
import org.litebridge.orm.api.select.ast.MergeNode;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.ast.UsingNode;
import org.litebridge.orm.api.select.ast.WhenNotMatchedNode;
import org.litebridge.orm.engine.LitebridgeContext;

import java.util.function.Function;

public class MergeWhenMatchedAndStep<DTO, MUS extends MergeUpdateStep<DTO>> extends MergeWhenMatchedStep<DTO, MUS> {

    public MergeWhenMatchedAndStep(final Table table,
                                   final MergeNode mergeNode,
                                   final QueryNode node,
                                   final LitebridgeContext litebridgeContext) {
        super(table, mergeNode, node, litebridgeContext);
    }

    public MergeWhenMatchedAndStep<DTO, MUS>.AdditionalWhen whenMatchedAnd(
            final Function<MergeAndStep<DTO, MUS>, MergeConditionClauseTerminal<DTO, MUS>> and,
            final Function<MUS, MergeTerminal> update) {
        //TODO: "and" condition
        final MUS mergeUpdateStep = createMergeUpdateStep();
        final MergeTerminal terminal = update.apply(mergeUpdateStep);
        return new AdditionalWhen(table, mergeNode, terminal.node(), litebridgeContext);
    }

    public final class AdditionalWhen extends MergeWhenMatchedAndStep<DTO, MUS> {

        private AdditionalWhen(final Table table,
                               final MergeNode mergeNode,
                               final QueryNode node,
                               final LitebridgeContext litebridgeContext) {
            super(table, mergeNode, node, litebridgeContext);
        }

        public MergeTerminal whenNotMatched(final Function<MergeInsertStep, InsertValuesStep> insert) {
            final MergeInsertStep mergeInsertStep = new MergeInsertStep(table, node, litebridgeContext);
            final InsertValuesStep insertValuesStep = insert.apply(mergeInsertStep);
            final QueryNode terminalNode = InsertValuesStepInspector.getNode(insertValuesStep);
            return new MergeTerminal(new WhenNotMatchedNode(node, (InsertValuesNode) terminalNode));
        }
    }
}
