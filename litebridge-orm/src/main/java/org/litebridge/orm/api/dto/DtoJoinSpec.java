package org.litebridge.orm.api.dto;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Table;
import org.litebridge.orm.api.select.model.JoinSpec;

import java.util.List;

public class DtoJoinSpec extends JoinSpec {

    private final Class<?> dtoClass;
    @Nullable
    private String dtoAlias;
    @Nullable
    private List<DtoSelectSpec.FieldColumn> fieldColumns;

    public DtoJoinSpec(final Class<?> dtoClass, final Table table) {
        super(table.schema(), table.name());
        this.dtoClass = dtoClass;
    }

    public Class<?> dtoClass() {
        return dtoClass;
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
