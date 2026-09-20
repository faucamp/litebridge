package org.litebridge.orm.api.select.sql;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.orm.api.select.model.ProtoExpressionResolver;
import org.litebridge.orm.expression.ColumnExpressionSpec;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.ProtoExpressionSpec;
import org.litebridge.orm.expression.Resolvable;
import org.litebridge.orm.expression.select.SelectColumnSpec;
import org.litebridge.orm.meta.QueryField;
import org.litebridge.orm.persistence.OrmTable;

import java.util.stream.Stream;

/**
 * Resolves proto-expressions into SQL-based select expressions.
 */
public final class SqlProtoExpressionResolver extends ProtoExpressionResolver {

    @Override
    protected ColumnExpressionSpec resolveSelectField(final Resolvable resolvable,
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
    protected Stream<ExpressionSpec> resolveSelectField(final QueryField queryField,
                                                        final @Nullable OrmTable ormTable,
                                                        final Table table,
                                                        final @Nullable String tableAlias,
                                                        final ClauseType clause) {
        throw new UnsupportedOperationException("QueryField not yet supported in SQL mode: " + queryField);
    }

    @Override
    protected Column getColumn(final Resolvable resolvable, final @Nullable OrmTable ormTable, final Table table, final ClauseType clause) {
        //TODO: SPI table/column registry
        return new Column(table, resolvable.column());
    }
}
