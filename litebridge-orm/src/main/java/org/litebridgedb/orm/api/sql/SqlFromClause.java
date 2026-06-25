package org.litebridgedb.orm.api.sql;

import org.litebridgedb.db.spi.Row;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.orm.api.select.FromClause;
import org.litebridgedb.orm.api.select.impl.ProtoExpressionResolver;
import org.litebridgedb.orm.expression.ExpressionSpec;
import org.litebridgedb.orm.persistence.TableRegistry;

import java.util.Arrays;
import java.util.List;

public final class SqlFromClause implements FromClause<Row,
        SqlFromClauseTerminal,
        SqlJoinClause,
        SqlJoinConditionClause,
        SqlJoinConditionClauseTerminal,
        SqlWhereConditionClause,
        SqlWhereConditionClauseTerminal,
        SqlGroupByClauseTerminal,
        SqlHavingConditionClause,
        SqlHavingConditionClauseTerminal,
        SqlOrderByClause,
        SqlOrderByClauseChain> {

    private final ExpressionSpec[] expressionSpecs;
    private final SqlSelectSpec selectSpec;
    private final TableRegistry tableRegistry;
    private final SqlSelector delegate;

    public SqlFromClause(final ExpressionSpec[] expressionSpecs,
                         final SqlSelectSpec selectSpec,
                         final TableRegistry tableRegistry,
                         final SqlSelector delegate) {
        this.expressionSpecs = expressionSpecs;
        this.selectSpec = selectSpec;
        this.tableRegistry = tableRegistry;
        this.delegate = delegate;
    }

    @Override
    public SqlFromClauseTerminal from(final String table) {
        final Table spiTable = tableRegistry.getOrCreateSpiTable(table);
        selectSpec.setTable(spiTable);

        if (expressionSpecs.length > 0) {
            final ProtoExpressionResolver protoExpressionResolver = new SqlProtoExpressionResolver(selectSpec);
            // Resolve all proto-SelectColumn expressions since we have the target table now
            final List<ExpressionSpec> resolvedExpressionSpecs = Arrays.stream(expressionSpecs)
                    .flatMap(protoExpressionResolver::resolveExpression)
                    .toList();

            selectSpec.setExpressions(resolvedExpressionSpecs);
        }

        return new SqlFromClauseTerminal(delegate);
    }
}
