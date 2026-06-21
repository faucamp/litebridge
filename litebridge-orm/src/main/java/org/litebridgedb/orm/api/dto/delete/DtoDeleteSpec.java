package org.litebridgedb.orm.api.dto.delete;

import org.litebridgedb.db.spi.Table;
import org.litebridgedb.orm.api.delete.model.DeleteSpec;
import org.litebridgedb.orm.api.dto.DtoDataSpec;
import org.litebridgedb.orm.api.select.model.SelectExpressionMapper;
import org.litebridgedb.orm.persistence.OrmTable;

public final class DtoDeleteSpec extends DeleteSpec implements DtoDataSpec {

    private final Class<?> dtoClass;
    private final OrmTable dtoTable;

    public DtoDeleteSpec(final Class<?> dtoClass, final OrmTable dtoTable, final SelectExpressionMapper selectExpressionMapper) {
        super(selectExpressionMapper);
        this.dtoClass = dtoClass;
        this.dtoTable = dtoTable;
        this.table = new Table(dtoTable.getMetaData().catalog(), dtoTable.getMetaData().schema(), dtoTable.getMetaData().name());
    }

    public Class<?> dtoClass() {
        return dtoClass;
    }

    @Override
    public OrmTable dtoTable() {
        return dtoTable;
    }
}
