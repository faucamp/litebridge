package org.litebridge.orm.api.select.dto;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.CollectionUtils;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.orm.api.select.model.ProtoExpressionResolver;
import org.litebridge.orm.expression.ColumnExpressionSpec;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.ProtoExpressionSpec;
import org.litebridge.orm.expression.Resolvable;
import org.litebridge.orm.expression.select.SelectColumnSpec;
import org.litebridge.orm.meta.QueryField;
import org.litebridge.orm.meta.QueryFieldInspector;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.orm.persistence.TableRegistry;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * Resolves proto-expressions into DTO-based select expressions.
 */
public final class DtoProtoExpressionResolver extends ProtoExpressionResolver {

    private final TableRegistry tableRegistry;

    /**
     * Creates a new instance of {@code DtoProtoExpressionResolver} without a select specification.
     *
     * @param tableRegistry the table registry
     */
    public DtoProtoExpressionResolver(final TableRegistry tableRegistry) {
        this.tableRegistry = tableRegistry;
    }

    @Override
    protected ColumnExpressionSpec resolveSelectColumnSpec(final Resolvable resolvable,
                                                           final @Nullable OrmTable ormTable,
                                                           final Table table,
                                                           final @Nullable String tableAlias,
                                                           final ClauseType clause) {
        final String alias;

        if (resolvable instanceof ProtoExpressionSpec protoExpressionSpec) {
            alias = protoExpressionSpec.alias();
        } else {
            alias = null;
        }

        return new SelectColumnSpec(getColumn(resolvable, ormTable, table, clause), alias, tableAlias);
    }

    @Override
    protected Stream<ExpressionSpec> resolveSelectColumnSpec(final QueryField queryField,
                                                             final @Nullable OrmTable ormTable,
                                                             final Table table,
                                                             final @Nullable String tableAlias,
                                                             final ClauseType clause) {
        final ExpressionSpec pendingExpressionSpec = QueryFieldInspector.getPendingExpressionSpec(queryField);

        if (pendingExpressionSpec != null) {
            return resolveExpression(pendingExpressionSpec, ormTable, table, tableAlias, clause);
        }

        // Map the input DTO field names to database column names
        final Class<?> dtoClass = QueryFieldInspector.getDtoClass(queryField);
        final String fieldName = QueryFieldInspector.getFieldName(queryField);
        final Column column = getColumn(dtoClass, fieldName, table, clause);
        return Stream.of(new SelectColumnSpec(column, null, tableAlias));
    }

    private Class<?> getDtoClass(final Resolvable resolvable, final @Nullable OrmTable ormTable) {
        if (resolvable instanceof ProtoExpressionSpec protoExpressionSpec
                && protoExpressionSpec.type() == SelectColumnSpec.class) {
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
        final ColumnMetaData columnMetaData = tableRegistry.getOrmTableOrThrow(dtoClass).columnMetaDataForField(fieldName);
        return columnMetaData.column();
    }
}
