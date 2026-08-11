package org.litebridge.orm.api.merge;

import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;

public class MergeAndStep<DTO, MUS extends MergeUpdateStep<DTO>> {

    private final QueryNode node;
    private final LitebridgeContext litebridgeContext;

    public MergeAndStep(final QueryNode node, final LitebridgeContext litebridgeContext) {
        this.node = node;
        this.litebridgeContext = litebridgeContext;
    }

    public MergeConditionClause<DTO, MUS> and(final String column) {
//        final Column spiColumn = litebridgeContext.tableMetaDataCache()
//                .ensureTableMetaData(node.table())
//                .column(column)
//                .toColumn();
//        return on(new SelectColumnSpec(spiColumn));
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public MergeConditionClause<DTO, MUS> and(final ExpressionSpec expression) {
//        return new MergeConditionClause<>(litebridgeContext,
//                LogicOperator.NOOP,
//                expression,
//                conditionNode -> new MergeConditionClauseTerminal<>(node));
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
