package org.litebridge.orm.api.merge;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Table;
import org.litebridge.orm.api.select.SelectTerminal;
import org.litebridge.orm.api.select.ast.MergeNode;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.SelectEngine;

import java.util.function.Function;

public abstract sealed class MergeUsingStep<DTO, MUS extends MergeUpdateStep<DTO>> permits DtoMergeUsingStep, SqlMergeUsingStep {

    protected final MergeNode mergeNode;
    protected final LitebridgeContext litebridgeContext;

    protected MergeUsingStep(final Class<DTO> dtoClass, final Table destinationTable, final LitebridgeContext litebridgeContext) {
        this.mergeNode = new MergeNode(destinationTable, dtoClass);
        this.litebridgeContext = litebridgeContext;
    }

    public MergeOnConditionClauseTerminal<DTO, MUS> using(final @Nullable Function<SelectEngine, SelectTerminal<?>> subselect) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
