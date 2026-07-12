package org.litebridge.orm.api.dto;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Table;
import org.litebridge.orm.api.select.impl.AbstractJoinSpec;
import org.litebridge.orm.api.select.model.SelectExpressionMapper;
import org.litebridge.orm.persistence.OrmTable;

import java.util.Collections;
import java.util.List;

public final class DtoJoinSpec extends AbstractJoinSpec implements DtoDataSpec {

    private final Class<?> dtoClass;
    private final OrmTable ormTable;

    private final SelectExpressionMapper selectExpressionMapper;
    @Nullable
    private List<DtoSelectSpec.FieldColumn> fieldColumns;

    public DtoJoinSpec(final Class<?> dtoClass, final OrmTable ormTable, final Table table, final SelectExpressionMapper selectExpressionMapper) {
        super(table, selectExpressionMapper);
        this.dtoClass = dtoClass;
        this.ormTable = ormTable;
        this.selectExpressionMapper = selectExpressionMapper;
    }

    public Class<?> dtoClass() {
        return dtoClass;
    }

    @Override
    public OrmTable dtoTable() {
        return ormTable;
    }

    public List<DtoSelectSpec.FieldColumn> getFieldColumns() {
        return fieldColumns != null ? fieldColumns : Collections.emptyList();
    }

    public void setFieldColumns(@Nullable final List<DtoSelectSpec.FieldColumn> fieldColumns) {
        this.fieldColumns = fieldColumns;
    }
}
