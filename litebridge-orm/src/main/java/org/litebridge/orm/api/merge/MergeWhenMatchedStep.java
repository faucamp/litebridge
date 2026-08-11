package org.litebridge.orm.api.merge;

import org.litebridge.db.spi.Table;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.engine.LitebridgeContext;

import java.util.function.Function;
import java.util.function.Supplier;

public class MergeWhenMatchedStep<DTO, MUS extends MergeUpdateStep<DTO>> {

    protected final Table table;
    protected final QueryNode node;
    protected final Supplier<MUS> mergeUpdateStepSupplier;
    protected final LitebridgeContext litebridgeContext;

    public MergeWhenMatchedStep(final Table table,
                                final QueryNode node,
                                final Supplier<MUS> mergeUpdateStepSupplier,
                                final LitebridgeContext litebridgeContext) {
        this.table = table;
        this.node = node;
        this.mergeUpdateStepSupplier = mergeUpdateStepSupplier;
        this.litebridgeContext = litebridgeContext;
    }

    public MergeWhenNotMatchedStep<DTO> whenMatched(final Function<MUS, MergeTerminal> update) {
        final MergeTerminal terminal = update.apply(mergeUpdateStepSupplier.get());
        //TODO: get terminal node
        return new MergeWhenNotMatchedStep<>(node);
    }
}
