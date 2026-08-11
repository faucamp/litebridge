package org.litebridge.orm.api.merge;

import org.litebridge.db.spi.Table;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.engine.LitebridgeContext;

import java.util.function.Function;
import java.util.function.Supplier;

public class MergeWhenMatchedAndStep<DTO, MUS extends MergeUpdateStep<DTO>> extends MergeWhenMatchedStep<DTO, MUS> {

    public MergeWhenMatchedAndStep(final Table table,
                                   final QueryNode node,
                                   final Supplier<MUS> mergeUpdateStepSupplier,
                                   final LitebridgeContext litebridgeContext) {
        super(table, node, mergeUpdateStepSupplier, litebridgeContext);
    }

    public MergeWhenMatchedAndStep<DTO, MUS>.AdditionalWhen whenMatchedAnd(
            final Function<MergeAndStep<DTO, MUS>, MergeConditionClauseTerminal<DTO, MUS>> and,
            final Function<MUS, MergeTerminal> update) {
        //TODO: "and" condition
        final MergeTerminal terminal = update.apply(mergeUpdateStepSupplier.get());
        return new AdditionalWhen(table, terminal.node(), mergeUpdateStepSupplier, litebridgeContext);
    }

    public final class AdditionalWhen extends MergeWhenMatchedAndStep<DTO, MUS> {

        private AdditionalWhen(final Table table,
                               final QueryNode node,
                               final Supplier<MUS> mergeUpdateStepSupplier,
                               final LitebridgeContext litebridgeContext) {
            super(table, node, mergeUpdateStepSupplier, litebridgeContext);
        }

        public void whenNotMatched(final Function<MergeInsertStep, MergeTerminal> insert) {
            throw new UnsupportedOperationException("Not yet implemented");
        }
    }
}
