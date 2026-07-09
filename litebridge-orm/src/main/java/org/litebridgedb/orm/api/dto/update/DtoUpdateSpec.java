package org.litebridgedb.orm.api.dto.update;

import org.litebridgedb.orm.api.dto.DtoDataSpec;
import org.litebridgedb.orm.api.select.model.SelectExpressionMapper;
import org.litebridgedb.orm.api.update.model.UpdateSpec;
import org.litebridgedb.orm.persistence.OrmTable;

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
