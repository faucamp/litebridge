package org.litebridge.orm.engine;

import org.litebridge.db.spi.update.UpdateResult;
import org.litebridge.orm.api.update.DtoUpdateStart;
import org.litebridge.orm.api.update.SqlUpdateStart;
import org.litebridge.orm.api.update.UpdateQuery;
import org.litebridge.orm.api.update.UpdateQueryInspector;
import org.litebridge.orm.engine.ast.QueryNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

public final class UpdateEngine extends AbstractUpdateEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateEngine.class);

    public <DTO> UpdateResult update(final Class<DTO> dtoClass,
                                     final Function<DtoUpdateStart<DTO>, UpdateQuery> update,
                                     final LitebridgeContext litebridgeContext) {
        final DtoUpdateStart<DTO> dtoDtoUpdateStart = new DtoUpdateStart<>(dtoClass, litebridgeContext);
        final UpdateQuery updateQuery = update.apply(dtoDtoUpdateStart);
        final QueryNode node = UpdateQueryInspector.getNode(updateQuery);
        return execute(node, litebridgeContext);
    }

    public UpdateResult update(final String tableName,
                               final Function<SqlUpdateStart, UpdateQuery> update,
                               final LitebridgeContext litebridgeContext) {
        final SqlUpdateStart sqlUpdateStart = new SqlUpdateStart(tableName, litebridgeContext);
        final UpdateQuery updateQuery = update.apply(sqlUpdateStart);
        final QueryNode node = UpdateQueryInspector.getNode(updateQuery);
        return execute(node, litebridgeContext);
    }

    @Override
    protected String operationTypeName() {
        return "UPDATE";
    }

    @Override
    protected Logger logger() {
        return LOGGER;
    }
}
