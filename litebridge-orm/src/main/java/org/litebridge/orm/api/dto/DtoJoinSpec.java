package org.litebridge.orm.api.dto;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Table;
import org.litebridge.orm.api.select.impl.AbstractJoinSpec;
import org.litebridge.orm.api.select.model.SelectExpressionMapper;
import org.litebridge.orm.persistence.OrmTable;

import java.util.Collections;
import java.util.List;

/**
 * Specification for a JOIN clause in a DTO-based query.
 */
public final class DtoJoinSpec extends AbstractJoinSpec implements DtoDataSpec {

    private final Class<?> dtoClass;
    private final OrmTable ormTable;

    private final SelectExpressionMapper selectExpressionMapper;
    @Nullable
    private List<DtoSelectSpec.FieldColumn> fieldColumns;

    /**
     * Creates a new instance of {@code DtoJoinSpec}.
     *
     * @param dtoClass the class of the DTO being joined
     * @param ormTable the metadata of the table being joined
     * @param table the database table representation
     * @param selectExpressionMapper the mapper for select expressions
     */
    public DtoJoinSpec(final Class<?> dtoClass, final OrmTable ormTable, final Table table, final SelectExpressionMapper selectExpressionMapper) {
        super(table, selectExpressionMapper);
        this.dtoClass = dtoClass;
        this.ormTable = ormTable;
        this.selectExpressionMapper = selectExpressionMapper;
    }

    /**
     * Returns the DTO class being joined.
     *
     * @return the DTO class
     */
    public Class<?> dtoClass() {
        return dtoClass;
    }

    @Override
    public OrmTable dtoTable() {
        return ormTable;
    }

    /**
     * Returns the list of field columns for this join.
     *
     * @return the list of field columns
     */
    public List<DtoSelectSpec.FieldColumn> getFieldColumns() {
        return fieldColumns != null ? fieldColumns : Collections.emptyList();
    }

    /**
     * Sets the list of field columns for this join.
     *
     * @param fieldColumns the list of field columns
     */
    public void setFieldColumns(@Nullable final List<DtoSelectSpec.FieldColumn> fieldColumns) {
        this.fieldColumns = fieldColumns;
    }
}
