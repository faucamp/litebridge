package org.litebridge.orm.api.dto.delete;

import org.litebridge.orm.api.delete.model.DeleteSpec;
import org.litebridge.orm.api.dto.DtoDataSpec;
import org.litebridge.orm.api.select.model.SelectExpressionMapper;
import org.litebridge.orm.persistence.OrmTable;

/**
 * Specification for a DTO-based delete operation.
 */
public final class DtoDeleteSpec extends DeleteSpec implements DtoDataSpec {

    private final Class<?> dtoClass;
    private final OrmTable dtoTable;

    /**
     * Creates a new DTO delete specification.
     *
     * @param dtoClass                the DTO class
     * @param dtoTable                the ORM table metadata
     * @param selectExpressionMapper the expression mapper
     */
    public DtoDeleteSpec(final Class<?> dtoClass, final OrmTable dtoTable, final SelectExpressionMapper selectExpressionMapper) {
        super(dtoTable.getMetaData().toTable(), selectExpressionMapper);
        this.dtoClass = dtoClass;
        this.dtoTable = dtoTable;
    }

    /**
     * Returns the DTO class.
     *
     * @return the DTO class
     */
    public Class<?> dtoClass() {
        return dtoClass;
    }

    @Override
    public OrmTable dtoTable() {
        return dtoTable;
    }
}
