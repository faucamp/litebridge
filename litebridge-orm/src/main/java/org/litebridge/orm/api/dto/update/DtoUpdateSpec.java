package org.litebridge.orm.api.dto.update;

import org.litebridge.db.spi.Table;
import org.litebridge.orm.api.delete.model.DeleteSpec;
import org.litebridge.orm.api.dto.DtoDataSpec;
import org.litebridge.orm.api.update.model.UpdateSpec;
import org.litebridge.orm.persistence.OrmTable;

public final class DtoUpdateSpec extends UpdateSpec implements DtoDataSpec {

    private final Class<?> dtoClass;
    private final OrmTable dtoTable;

    public DtoUpdateSpec(final Class<?> dtoClass, final OrmTable dtoTable) {
        this.dtoClass = dtoClass;
        this.dtoTable = dtoTable;
        this.tableMetaData = dtoTable.getMetaData();
    }

    public Class<?> dtoClass() {
        return dtoClass;
    }

    @Override
    public OrmTable dtoTable() {
        return dtoTable;
    }
}
