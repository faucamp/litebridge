package org.litebridge.orm.api.update;

import org.litebridge.orm.api.dto.update.DtoUpdater;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.sql.update.SqlUpdater;

public final class UpdateQueryInspector {

    private UpdateQueryInspector() {
    }

    public static QueryNode getNode(final UpdateQuery updateQuery) {
        return switch (updateQuery) {
            case DtoUpdater dtoUpdater -> dtoUpdater.node();
            case SqlUpdater sqlUpdater -> sqlUpdater.node();
            default -> throw new UnsupportedOperationException("Unsupported update query type: " + updateQuery.getClass());
        };
    }
}
