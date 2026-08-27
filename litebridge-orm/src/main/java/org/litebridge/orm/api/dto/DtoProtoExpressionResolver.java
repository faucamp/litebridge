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
import org.litebridge.orm.meta.QueryField;
import org.litebridge.orm.meta.QueryFieldInspector;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.orm.persistence.TableRegistry;
import org.litebridge.orm.persistence.alias.AliasGenerator;
import org.litebridge.tracking.ClassFieldAccessorCache;
import org.litebridge.tracking.FieldAccessor;

import java.util.Objects;

/**
 * Resolves proto-expressions into DTO-based select expressions.
 */
public final class DtoProtoExpressionResolver extends ProtoExpressionResolver {

    @Deprecated(forRemoval = true)
    private @Nullable DtoSelectSpec selectSpec;
    private final AliasGenerator aliasGenerator;
    private final ClassFieldAccessorCache classFieldAccessorCache;
    private final TableRegistry tableRegistry;

    /**
     * Creates a new instance of {@code DtoProtoExpressionResolver}.
     *
     * @param selectSpec              the select specification
     * @param aliasGenerator          the alias generator
     * @param classFieldAccessorCache the field accessor cache
     * @param tableRegistry           the table registry
     */
    @Deprecated(forRemoval = true)
    public DtoProtoExpressionResolver(final DtoSelectSpec selectSpec,
                                      final AliasGenerator aliasGenerator,
                                      final ClassFieldAccessorCache classFieldAccessorCache,
                                      final TableRegistry tableRegistry) {
        this.selectSpec = selectSpec;
        this.aliasGenerator = aliasGenerator;
        this.classFieldAccessorCache = classFieldAccessorCache;
        this.tableRegistry = tableRegistry;
    }

    /**
     * Creates a new instance of {@code DtoProtoExpressionResolver} without a select specification.
     *
     * @param aliasGenerator          the alias generator
     * @param classFieldAccessorCache the field accessor cache
     * @param tableRegistry           the table registry
     */
    public DtoProtoExpressionResolver(final AliasGenerator aliasGenerator,
                                      final ClassFieldAccessorCache classFieldAccessorCache,
                                      final TableRegistry tableRegistry) {
        this.aliasGenerator = aliasGenerator;
        this.classFieldAccessorCache = classFieldAccessorCache;
        this.tableRegistry = tableRegistry;
    }

    @Override
    protected ColumnExpressionSpec resolveSelectField(final Resolvable resolvable, final @Nullable OrmTable ormTable, final Table table, final ClauseType clause) {
        // Map the input DTO field names to database column names
        final Class<?> dtoClass = getDtoClass(resolvable, ormTable);
        final Column column = getColumn(dtoClass, resolvable, table, clause);
        final FieldAccessor fieldAccessor = classFieldAccessorCache.fieldAccessorOrThrow(dtoClass, resolvable.column());
        return new SelectFieldSpec(fieldAccessor, column);
    }

    @Override
    protected ColumnExpressionSpec resolveSelectField(final QueryField queryField, final @Nullable OrmTable ormTable, final Table table, final ClauseType clause) {
        // Map the input DTO field names to database column names
        final String fieldName = QueryFieldInspector.getFieldName(queryField);
        final Column column = getColumn(QueryFieldInspector.getDtoClass(queryField), fieldName, table, clause);
        final FieldAccessor fieldAccessor = classFieldAccessorCache.fieldAccessorOrThrow(QueryFieldInspector.getDtoClass(queryField), fieldName);
        return new SelectFieldSpec(fieldAccessor, column);
    }

    private Class<?> getDtoClass(final Resolvable resolvable, final @Nullable OrmTable ormTable) {
        if (resolvable instanceof ProtoExpressionSpec protoExpressionSpec
                && protoExpressionSpec.type() == SelectFieldSpec.class) {
            final Object[] args = protoExpressionSpec.args();

            if (!CollectionUtils.isEmpty(args)) {
                return (Class<?>) args[0];
            }
        }

        return Objects.requireNonNull(ormTable).dtoClass();
    }

    @Override
    protected Column getColumn(final Resolvable resolvable, final @Nullable OrmTable ormTable, final Table table, final ClauseType clause) {
        return getColumn(getDtoClass(resolvable, ormTable), resolvable, table, clause);
    }

    private Column getColumn(final Class<?> dtoClass, final Resolvable resolvable, final Table table, final ClauseType clause) {
        return getColumn(dtoClass, resolvable.column(), table, clause);
    }

    private Column getColumn(final Class<?> dtoClass, final String fieldName, Table table, final ClauseType clause) {
        // Map the input DTO field names to database column names
        if (selectSpec != null) {
            final OrmTable ormTable = tableRegistry.getTableOrThrow(dtoClass);

            if (ormTable.equals(selectSpec.dtoTable())) {
                table = selectSpec.getTable();
            } else {
                table = Objects.requireNonNull(selectSpec.getJoins()).stream()
                        .filter(DtoJoinSpec.class::isInstance)
                        .map(DtoJoinSpec.class::cast)
                        .filter(join -> join.dtoTable().equals(ormTable))
                        .map(org.litebridge.orm.api.select.model.JoinSpec::table)
                        .findFirst()
                        .orElseGet(() -> ormTable.getMetaData().toTable());
            }
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
