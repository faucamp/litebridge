package org.litebridgedb.orm.api.sql;

import org.litebridgedb.db.spi.Row;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.orm.api.select.FromClause;
import org.litebridgedb.orm.expression.Expression;
import org.litebridgedb.orm.expression.ProtoExpression;
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
        SqlOrderByClause,
        SqlOrderByClauseChain> {

    private final Expression[] expressions;
    private final SqlSelectSpec selectSpec;
    private final TableRegistry tableRegistry;
    private final SqlSelector delegate;

    public SqlFromClause(final Expression[] expressions,
                         final SqlSelectSpec selectSpec,
                         final TableRegistry tableRegistry,
                         final SqlSelector delegate) {
        this.expressions = expressions;
        this.selectSpec = selectSpec;
        this.tableRegistry = tableRegistry;
        this.delegate = delegate;
    }

    @Override
    public SqlFromClauseTerminal from(final String table) {
        final Table spiTable = tableRegistry.getOrCreateSpiTable(table);
        selectSpec.setTable(spiTable);

        if (expressions.length > 0) {
            // Resolve all proto-SelectColumn expressions since we have the target table now
            final List<Expression> resolvedExpressions = Arrays.stream(expressions)
                    .map(expression -> {
                        if (expression instanceof ProtoExpression protoExpression) {
                            return protoExpression.resolve(spiTable);
                        } else {
                            return expression;
                        }
                    })
                    .toList();

            selectSpec.setExpressions(resolvedExpressions);
        }

        return new SqlFromClauseTerminal(delegate);
    }
}
