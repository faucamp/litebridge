package org.litebridge.orm.engine;

import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.update.UpdateResult;
import org.litebridge.orm.api.merge.DtoMergeUsingStep;
import org.litebridge.orm.api.merge.MergeTerminal;
import org.litebridge.orm.api.merge.MergeTerminalInspector;
import org.litebridge.orm.api.merge.SqlMergeUsingStep;
import org.litebridge.orm.engine.ast.QueryNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;
import java.util.function.Supplier;

public final class MergeEngine extends AbstractInsertEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(MergeEngine.class);

    public <DTO> UpdateResult mergeInto(final Class<DTO> dtoClass,
                                        final Function<DtoMergeUsingStep<DTO>, MergeTerminal> merge,
                                        final LitebridgeContext litebridgeContext) {
        final DtoMergeUsingStep<DTO> mergeUsingStep = new DtoMergeUsingStep<>(dtoClass, litebridgeContext);
        final MergeTerminal mergeTerminal = merge.apply(mergeUsingStep);
        return execute(mergeTerminal,
                () -> litebridgeContext.tableRegistry().getOrmTableOrThrow(dtoClass).getMetaData().toTable(),
                litebridgeContext);
    }

    public UpdateResult mergeInto(final String tableName,
                                  final Function<SqlMergeUsingStep, MergeTerminal> merge,
                                  final LitebridgeContext litebridgeContext) {
        final SqlMergeUsingStep mergeUsingStep = new SqlMergeUsingStep(tableName, litebridgeContext);
        final MergeTerminal mergeTerminal = merge.apply(mergeUsingStep);
        return execute(mergeTerminal,
                () -> litebridgeContext.tableRegistry().getOrCreateSpiTable(tableName),
                litebridgeContext);
    }

    @Override
    protected String operationTypeName() {
        return "MERGE";
    }

    @Override
    protected Logger logger() {
        return LOGGER;
    }

    private UpdateResult execute(final MergeTerminal mergeTerminal, final Supplier<Table> tableSupplier, final LitebridgeContext litebridgeContext) {
        final QueryNode node = MergeTerminalInspector.getNode(mergeTerminal);
        return execute(node, preparedOperation -> createUpdateMetaData(preparedOperation, tableSupplier, litebridgeContext), UpdateResult.class, litebridgeContext);
    }
}
