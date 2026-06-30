package org.litebridgedb.orm.api.dto;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.commons.CollectionUtils;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.orm.api.select.model.ProtoExpressionResolver;
import org.litebridgedb.orm.expression.ColumnExpressionSpec;
import org.litebridgedb.orm.expression.ProtoExpressionSpec;
import org.litebridgedb.orm.expression.Resolvable;
import org.litebridgedb.orm.expression.select.SelectFieldSpec;
import org.litebridgedb.orm.persistence.OrmTable;
import org.litebridgedb.orm.persistence.TableRegistry;
import org.litebridgedb.orm.persistence.alias.AliasGenerator;
import org.litebridgedb.tracking.ClassFieldAccessorCache;
import org.litebridgedb.tracking.FieldAccessor;

import java.util.Objects;

public final class DtoProtoExpressionResolver extends ProtoExpressionResolver {

    private @Nullable DtoSelectSpec selectSpec;
    private final AliasGenerator aliasGenerator;
    private final ClassFieldAccessorCache classFieldAccessorCache;
    private final TableRegistry tableRegistry;

    public DtoProtoExpressionResolver(final DtoSelectSpec selectSpec,
                                      final AliasGenerator aliasGenerator,
                                      final ClassFieldAccessorCache classFieldAccessorCache,
                                      final TableRegistry tableRegistry) {
        this.selectSpec = selectSpec;
        this.aliasGenerator = aliasGenerator;
        this.classFieldAccessorCache = classFieldAccessorCache;
        this.tableRegistry = tableRegistry;
    }

    public DtoProtoExpressionResolver(final AliasGenerator aliasGenerator,
                                      final ClassFieldAccessorCache classFieldAccessorCache,
                                      final TableRegistry tableRegistry) {
        this.aliasGenerator = aliasGenerator;
        this.classFieldAccessorCache = classFieldAccessorCache;
        this.tableRegistry = tableRegistry;
    }

    public @Nullable DtoSelectSpec getSelectSpec() {
        return selectSpec;
    }

    public void setSelectSpec(final DtoSelectSpec selectSpec) {
        this.selectSpec = selectSpec;
    }

    @Override
    protected ColumnExpressionSpec resolveSelectField(final Resolvable resolvable) {
        // Map the input DTO field names to database column names
        Objects.requireNonNull(selectSpec, "SelectSpec not set");
        final Class<?> dtoClass = getDtoClass(resolvable);
        final Column column = getColumn(dtoClass, resolvable);
        final FieldAccessor fieldAccessor = classFieldAccessorCache.fieldAccessorOrThrow(dtoClass, resolvable.column());
        return new SelectFieldSpec(fieldAccessor, column);
    }

    private Class<?> getDtoClass(final Resolvable resolvable) {
        if (resolvable instanceof ProtoExpressionSpec protoExpressionSpec
                && protoExpressionSpec.type() == SelectFieldSpec.class) {
            final Object[] args = protoExpressionSpec.args();

            if (!CollectionUtils.isEmpty(args)) {
                return (Class<?>) args[0];
            }
        }

        return selectSpec.dtoClass();
    }

    @Override
    protected Column getColumn(final Resolvable resolvable) {
        return getColumn(getDtoClass(resolvable), resolvable);
    }

    private Column getColumn(final Class<?> dtoClass, final Resolvable resolvable) {
        // Map the input DTO field names to database column names
        final String fieldName = resolvable.column();
        final OrmTable ormTable = tableRegistry.getTableOrThrow(dtoClass);
        final Table table;

        if (ormTable.equals(selectSpec.dtoTable())) {
            table = selectSpec.getTable();
        } else {
            table = ormTable.getMetaData().toTable();
        }

        final ColumnMetaData columnMetaData = tableRegistry.getTableOrThrow(dtoClass).getColumnForFieldName(resolvable.column());
        return aliasGenerator.aliasColumn(table, columnMetaData);
    }
}
