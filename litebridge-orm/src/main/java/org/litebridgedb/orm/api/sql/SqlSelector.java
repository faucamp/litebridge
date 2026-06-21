package org.litebridgedb.orm.api.sql;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.commons.CollectionUtils;
import org.litebridgedb.db.spi.Row;
import org.litebridgedb.db.spi.convert.TypeConverter;
import org.litebridgedb.orm.api.select.impl.AbstractSelector;
import org.litebridgedb.orm.api.select.impl.LitebridgeContext;
import org.litebridgedb.orm.config.LitebridgeConfig;
import org.litebridgedb.orm.expression.ExpressionSpec;
import org.litebridgedb.orm.persistence.TableRegistry;
import org.litebridgedb.orm.persistence.TransactionalDatabaseProvider;

import java.util.List;

public final class SqlSelector extends AbstractSelector<Row, SqlSelectSpec> {

    private final TableRegistry tableRegistry;

    public SqlSelector(final TransactionalDatabaseProvider databaseProvider,
                       final TableRegistry tableRegistry,
                       final LitebridgeContext litebridgeContext) {
        super(new SqlSelectSpec(litebridgeContext), databaseProvider, Row.class, litebridgeContext);
        this.tableRegistry = tableRegistry;
    }

    public SqlFromClause select(final ExpressionSpec... expressionSpecs) {
        return new SqlFromClause(expressionSpecs, selectSpec, tableRegistry, this);
    }

    @Override
    public @Nullable Row oneOrNull() {
        return fetchOneRecord(false);
    }

    @Override
    public @Nullable Row firstOrNull() {
        return fetchOneRecord(true);
    }

    @Override
    public List<Row> list() {
        return executeQuery();
    }

    @Override
    protected List<Row> executeQuery() {
        if (selectSpec.getValueTypeOverride() == null) {
            return executeQuery(selectSpec);
        } else {
            final List<Row> rows = executeQuery(selectSpec);
            final TypeConverter typeConverter = databaseProvider.getTypeConverter();
            final Class<?> valueTypeOverride = selectSpec.getValueTypeOverride();

            // Transforms row column values to the specified override type
            return rows.stream()
                    .peek(row -> row.columnStream()
                            .forEach(rowColumn -> row.updateColumn(rowColumn.column(), typeConverter.convert(rowColumn.value(), valueTypeOverride))))
                    .toList();
        }
    }

    private @Nullable Row fetchOneRecord(final boolean first) {
        if (first) {
            // Set LIMIT since we are only interested in the first record
            selectSpec.ensureLimit().setLimit(1);
        }

        final List<Row> resultList = executeQuery();

        if (CollectionUtils.isEmpty(resultList)) {
            return null;
        }

        if (!first && resultList.size() > 1) {
            throw new IllegalStateException("Expected exactly one result, but got %d".formatted(resultList.size()));
        }

        return resultList.getFirst();
    }
}
