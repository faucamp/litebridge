package org.litebridge.orm.api.dto;

import org.jspecify.annotations.Nullable;
import org.litebridge.orm.api.select.model.JoinSpec;
import org.litebridge.orm.persistence.OrmTable;

import java.util.List;

public final class DtoJoinSpec extends JoinSpec implements DtoDataSpec {

    private final Class<?> dtoClass;
    private final OrmTable dtoTable;
    @Nullable
    private String dtoAlias;
    @Nullable
    private List<DtoSelectSpec.FieldColumn> fieldColumns;

    public DtoJoinSpec(final Class<?> dtoClass, final OrmTable table) {
        super(table.getMetaData().schema(), table.getMetaData().name());
        this.dtoClass = dtoClass;
        this.dtoTable = table;
    }

    public Class<?> dtoClass() {
        return dtoClass;
    }

    @Override
    public OrmTable dtoTable() {
        return dtoTable;
    }

    public @Nullable String getDtoAlias() {
        return dtoAlias;
    }

    public void setDtoAlias(final @Nullable String dtoAlias) {
        this.dtoAlias = dtoAlias;
    }

    public @Nullable List<DtoSelectSpec.FieldColumn> getFieldColumns() {
        return fieldColumns;
    }

    public void setFieldColumns(@Nullable final List<DtoSelectSpec.FieldColumn> fieldColumns) {
        this.fieldColumns = fieldColumns;
    }
}
