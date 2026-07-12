package org.litebridge.orm.api.dto;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.CollectionUtils;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.orm.api.select.model.ProtoExpressionResolver;
import org.litebridge.orm.expression.ColumnExpressionSpec;
import org.litebridge.orm.expression.ProtoExpressionSpec;
import org.litebridge.orm.expression.Resolvable;
import org.litebridge.orm.expression.select.SelectFieldSpec;
import org.litebridge.orm.meta.QFInspector;
import org.litebridge.orm.meta.QueryField;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.orm.persistence.TableRegistry;
import org.litebridge.orm.persistence.alias.AliasGenerator;
import org.litebridge.tracking.ClassFieldAccessorCache;
import org.litebridge.tracking.FieldAccessor;

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

    @Override
    protected ColumnExpressionSpec resolveSelectField(final Resolvable resolvable, final ClauseType clause) {
        // Map the input DTO field names to database column names
        final Class<?> dtoClass = getDtoClass(resolvable);
        final Column column = getColumn(dtoClass, resolvable, clause);
        final FieldAccessor fieldAccessor = classFieldAccessorCache.fieldAccessorOrThrow(dtoClass, resolvable.column());
        return new SelectFieldSpec(fieldAccessor, column);
    }

    @Override
    protected ColumnExpressionSpec resolveSelectField(final QueryField queryField, final ClauseType clause) {
        // Map the input DTO field names to database column names
        final String fieldName = QFInspector.getFieldName(queryField);
        final Column column = getColumn(QFInspector.getDtoClass(queryField), fieldName, clause);
        final FieldAccessor fieldAccessor = classFieldAccessorCache.fieldAccessorOrThrow(QFInspector.getDtoClass(queryField), fieldName);
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

        return Objects.requireNonNull(selectSpec).dtoClass();
    }

    @Override
    protected Column getColumn(final Resolvable resolvable, final ClauseType clause) {
        return getColumn(getDtoClass(resolvable), resolvable, clause);
    }

    private Column getColumn(final Class<?> dtoClass, final Resolvable resolvable, final ClauseType clause) {
        return getColumn(dtoClass, resolvable.column(), clause);
    }

    private Column getColumn(final Class<?> dtoClass, final String fieldName, final ClauseType clause) {
        // Map the input DTO field names to database column names
        final OrmTable ormTable = tableRegistry.getTableOrThrow(dtoClass);
        final Table table;

        if (selectSpec != null && ormTable.equals(selectSpec.dtoTable())) {
            table = selectSpec.getTable();
        } else {
            table = ormTable.getMetaData().toTable();
        }

        final ColumnMetaData columnMetaData = tableRegistry.getTableOrThrow(dtoClass).getColumnForFieldName(fieldName);

        if (clause == ClauseType.SELECT) {
            return aliasGenerator.aliasColumn(table, columnMetaData);
        } else if (selectSpec != null) {
            // Match the column to a selected one to inherit the alias if possible
            return selectSpec.getExpressions().stream()
                    .filter(expressionSpec -> expressionSpec instanceof ColumnExpressionSpec)
                    .map(ColumnExpressionSpec.class::cast)
                    .map(ColumnExpressionSpec::getColumn)
                    .filter(selectedColumn -> selectedColumn.table().equalsIgnoreAlias(columnMetaData.table())
                            && selectedColumn.name().equals(columnMetaData.name()))
                    .findAny().orElseGet(columnMetaData::toColumn);
        } else {
            return columnMetaData.toColumn();
        }
    }
}
