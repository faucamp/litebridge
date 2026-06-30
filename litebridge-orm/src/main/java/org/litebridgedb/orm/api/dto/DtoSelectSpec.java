package org.litebridgedb.orm.api.dto;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.orm.api.select.model.SelectSpec;
import org.litebridgedb.orm.engine.LitebridgeContext;
import org.litebridgedb.orm.expression.ExpressionSpec;
import org.litebridgedb.orm.expression.select.SelectFieldSpec;
import org.litebridgedb.orm.persistence.OrmTable;
import org.litebridgedb.orm.persistence.alias.AliasGenerator;
import org.litebridgedb.tracking.FieldAccessor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class DtoSelectSpec extends SelectSpec implements DtoDataSpec {

    private final Class<?> dtoClass;
    private final OrmTable dtoTable;
    private final @Nullable Class<?> typeOverride;

    public DtoSelectSpec(final Class<?> dtoClass,
                         final OrmTable dtoTable,
                         final AliasGenerator aliasGenerator,
                         final LitebridgeContext litebridgeContext,
                         final @Nullable Class<?> typeOverride) {
        super(litebridgeContext);
        this.dtoClass = dtoClass;
        this.dtoTable = dtoTable;
        this.table = aliasGenerator.aliasTable(dtoTable);
        this.typeOverride = typeOverride;
    }

    public DtoSelectSpec(final Class<?> dtoClass,
                         final OrmTable dtoTable,
                         final AliasGenerator aliasGenerator,
                         final LitebridgeContext litebridgeContext) {
        this(dtoClass, dtoTable, aliasGenerator, litebridgeContext, null);
    }

    public Class<?> dtoClass() {
        return dtoTable.dtoClass();
    }

    @Override
    public OrmTable dtoTable() {
        return dtoTable;
    }

    public @Nullable Class<?> typeOverride() {
        return typeOverride;
    }

    public DtoJoinSpec newJoinSpec(final Class<?> dtoClass, final OrmTable ormTable, final Table table) {
        return addNewJoinSpecBefore(null, dtoClass, ormTable, table);
    }

    public DtoJoinSpec newJoinSpecBefore(final DtoJoinSpec other, final Class<?> dtoClass, final OrmTable ormTable, final Table table) {
        return addNewJoinSpecBefore(other, dtoClass, ormTable, table);
    }

    private DtoJoinSpec addNewJoinSpecBefore(final @Nullable DtoJoinSpec other, final Class<?> dtoClass, final OrmTable ormTable, final Table table) {
        if (this.joins == null) {
            joins = new ArrayList<>();
        }

        final DtoJoinSpec joinSpec = new DtoJoinSpec(dtoClass, ormTable, table, selectExpressionMapper());

        if (other != null) {
            joins.add(joins.indexOf(other), joinSpec);
        } else {
            joins.add(joinSpec);
        }

        return joinSpec;
    }

    public List<ExpressionSpec> createSelectFieldSpecs(final String[] fields) {
        return Arrays.stream(fields)
                .map(this::createSelectFieldSpec)
                .toList();
    }

    private ExpressionSpec createSelectFieldSpec(final String field) {
        final ColumnMetaData columnMetaData = dtoTable.getColumnForFieldName(field);
        final FieldAccessor fieldAccessor = dtoTable.getFieldForColumnName(columnMetaData.name());
        return new SelectFieldSpec(fieldAccessor, columnMetaData.toColumn());
    }

    public record FieldColumn(FieldAccessor fieldAccessor, Column column) {
    }
}
