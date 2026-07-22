package org.litebridge.orm.api.sql;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.CollectionUtils;
import org.litebridge.db.spi.Row;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.ast.SelectNode;
import org.litebridge.orm.api.select.impl.AbstractSelector;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.persistence.TableRegistry;
import org.litebridge.orm.persistence.TransactionalDatabaseProvider;

import java.util.Arrays;
import java.util.List;

public final class SqlSelector extends AbstractSelector<Row, SqlSelectSpec> {

    private final TableRegistry tableRegistry;

    public SqlSelector(final TransactionalDatabaseProvider databaseProvider,
                       final TableRegistry tableRegistry,
                       final LitebridgeContext litebridgeContext,
                       final QueryNode node) {
        this(new SqlSelectSpec(litebridgeContext), databaseProvider, tableRegistry, litebridgeContext, node);
    }

    private SqlSelector(final SqlSelectSpec selectSpec,
                        final TransactionalDatabaseProvider databaseProvider,
                        final TableRegistry tableRegistry,
                        final LitebridgeContext litebridgeContext,
                        final QueryNode node) {
        super(selectSpec, databaseProvider, tableRegistry, Row.class, litebridgeContext, node);
        this.tableRegistry = tableRegistry;
        if (!selectSpec.isSelectExpressionMapperSet()) {
            selectSpec.setProtoExpressionResolver(new SqlProtoExpressionResolver(selectSpec));
        }
    }

    @Override
    protected SqlSelectSpec createSelectSpec(final org.litebridge.orm.persistence.alias.AliasGenerator aliasGenerator) {
        final LitebridgeContext freshContext = new LitebridgeContext(
                litebridgeContext.config(),
                litebridgeContext.fromClauseEngine(),
                litebridgeContext.sqlFunctionRegistry(),
                litebridgeContext.queryPlanCache(),
                aliasGenerator
        );
        final SqlSelectSpec selectSpec = new SqlSelectSpec(freshContext);
        selectSpec.setProtoExpressionResolver(new SqlProtoExpressionResolver(selectSpec));
        return selectSpec;
    }

    public SqlFromClause select(final ExpressionSpec... expressionSpecs) {
        final QueryNode selectNode = new SelectNode(node, expressionSpecs, null);
        return new SqlFromClause(tableRegistry, withNode(selectNode));
    }

    @Override
    public SqlSelector withNode(final QueryNode node) {
        return new SqlSelector(selectSpec, databaseProvider, tableRegistry, litebridgeContext, node);
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
        return executeQuery(selectSpec);
    }

    private @Nullable Row fetchOneRecord(final boolean first) {
        final List<Row> resultList;
        if (first) {
            resultList = ((SqlSelector) withNode(new org.litebridge.orm.api.select.ast.LimitNode(node, java.util.Optional.of(1), java.util.Optional.empty()))).executeQuery();
        } else {
            resultList = executeQuery();
        }

        if (CollectionUtils.isEmpty(resultList)) {
            return null;
        }

        if (!first && resultList.size() > 1) {
            throw new IllegalStateException("Expected exactly one result, but got %d".formatted(resultList.size()));
        }

        return resultList.getFirst();
    }
    @Override
    public SqlSelectSpec selectSpec() {
        return (SqlSelectSpec) super.selectSpec();
    }
}
