package org.litebridge.orm.api.sql;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.Table;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.api.select.impl.AbstractSelector;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.persistence.TableRegistry;
import org.litebridge.orm.persistence.TransactionalDatabaseProvider;
import org.litebridge.orm.persistence.alias.AliasGenerator;

import java.util.List;

@Deprecated(forRemoval = true)
public final class SqlSelector extends AbstractSelector<Row, SqlSelectSpec> {

    private final TableRegistry tableRegistry;
    private final Table table;

    public SqlSelector(final Table table,
                       final TransactionalDatabaseProvider databaseProvider,
                       final TableRegistry tableRegistry,
                       final LitebridgeContext litebridgeContext,
                       final QueryNode node) {
        super(databaseProvider, tableRegistry, Row.class, litebridgeContext, node);
        this.tableRegistry = tableRegistry;
        this.table = table;
    }

    @Override
    protected SqlSelectSpec createSelectSpec(final AliasGenerator aliasGenerator) {
//        final SqlSelectSpec selectSpec = new SqlSelectSpec(litebridgeContext, table);
//        selectSpec.setProtoExpressionResolver(new SqlProtoExpressionResolver(selectSpec));
//        return selectSpec;
        throw new UnsupportedOperationException("Deprecated");
    }

    public SqlFromClause select(final ExpressionSpec... expressionSpecs) {
//        final QueryNode selectNode = new SelectNode(node, expressionSpecs, null);
//        return new SqlFromClause(withNode(selectNode));
        throw new UnsupportedOperationException("Deprecated");
    }

    @Override
    public SqlSelector withNode(final QueryNode node) {
        this.node = node;
        return this;
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

    private @Nullable Row fetchOneRecord(final boolean first) {
//        final List<Row> resultList;
//
//        if (first) {
//            resultList = withNode(new LimitNode(node, Optional.of(1), Optional.empty())).executeQuery();
//        } else {
//            resultList = executeQuery();
//        }
//
//        if (CollectionUtils.isEmpty(resultList)) {
//            return null;
//        }
//
//        if (!first && resultList.size() > 1) {
//            throw new IllegalStateException("Expected exactly one result, but got %d".formatted(resultList.size()));
//        }
//
//        return resultList.getFirst();
        throw new UnsupportedOperationException("Deprecated");
    }

    Table table() {
        return table;
    }
}
