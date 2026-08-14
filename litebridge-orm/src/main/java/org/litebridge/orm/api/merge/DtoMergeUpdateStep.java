package org.litebridge.orm.api.merge;

import org.litebridge.db.spi.Table;
import org.litebridge.orm.api.dto.update.DtoUpdateStart;
import org.litebridge.orm.api.dto.update.DtoUpdater;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.ast.UsingNode;
import org.litebridge.orm.api.update.UpdateQuery;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.persistence.OrmTable;

import java.util.function.Function;

public final class DtoMergeUpdateStep<DTO> extends MergeUpdateStep<DTO> {

    private final Class<DTO> dtoClass;

    public DtoMergeUpdateStep(final Class<DTO> dtoClass, final Table table, final QueryNode node, final LitebridgeContext litebridgeContext) {
        super(table, node, litebridgeContext);
        this.dtoClass = dtoClass;
    }

    public MergeTerminal update(final Function<DtoUpdateStart<DTO>, UpdateQuery> update) {
        final OrmTable ormTable = litebridgeContext.tableRegistry().getOrmTableOrThrow(table);
        final DtoUpdater<DTO> dtoUpdater = new DtoUpdater<>(dtoClass, ormTable, litebridgeContext);
        final UpdateQuery updateQuery = update.apply(dtoUpdater);
        //TODO: get output node
        return new MergeTerminal(null);
    }

    public MergeTerminal delete() {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
