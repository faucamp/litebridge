package org.litebridge.orm.api.dto.update;

import org.litebridge.orm.api.dto.DtoDataSpec;
import org.litebridge.orm.api.select.model.SelectExpressionMapper;
import org.litebridge.orm.api.update.model.UpdateSpec;
import org.litebridge.orm.persistence.OrmTable;

public final class DtoUpdateSpec extends UpdateSpec implements DtoDataSpec {

    private final Class<?> dtoClass;
    private final OrmTable dtoTable;

    public DtoUpdateSpec(final Class<?> dtoClass, final OrmTable dtoTable, final SelectExpressionMapper selectExpressionMapper) {
        super(dtoTable.getMetaData().toTable(), selectExpressionMapper);
        this.dtoClass = dtoClass;
        this.dtoTable = dtoTable;
    }

    public Class<?> dtoClass() {
        return dtoClass;
    }

    @Override
    public OrmTable dtoTable() {
        return dtoTable;
    }
}
