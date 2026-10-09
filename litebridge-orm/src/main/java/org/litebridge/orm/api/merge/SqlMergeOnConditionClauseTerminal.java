package org.litebridge.orm.api.merge;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.condition.CbConditionClauseTerminalInspector;
import org.litebridge.orm.api.condition.CbSqlConditionClauseTerminal;
import org.litebridge.orm.api.condition.SqlConditionClauseStart;
import org.litebridge.orm.api.condition.SqlQueryConditionBuilder;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.ConditionGroupNode;
import org.litebridge.orm.engine.ast.MergeNode;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.expression.select.ValuesSpec;

public final class SqlMergeOnConditionClauseTerminal
        extends MergeOnConditionClauseTerminal<Row,
        SqlMergeOnConditionClauseTerminal,
        SqlMergeUpdateStep,
        MergeInsertStep,
        SqlQueryConditionBuilder> {

    /**
     * Creates a new {@code MergeOnConditionClauseTerminal} instance.
     *
     * @param usingTable        Table to use as the merge source.
     * @param usingQueryNode    Subquery to merge on.
     * @param usingValues       Values to merge on.
     * @param usingAlias        Alias to use for the merge source.
     * @param on                The using on condition clause query node.
     * @param mergeNode         The root merge query node.
     * @param litebridgeContext Current Litebridge context.
     */
    public SqlMergeOnConditionClauseTerminal(final String usingTable, final @Nullable QueryNode usingQueryNode, final @Nullable ValuesSpec usingValues, final @Nullable String usingAlias, final QueryNode on, final MergeNode mergeNode, final LitebridgeContext litebridgeContext) {
        super(usingTable, null, usingQueryNode, usingValues, usingAlias, on, mergeNode, litebridgeContext);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    protected SqlMergeOnConditionClauseTerminal onImpl(final LogicOperator logicOperator, final SqlQueryConditionBuilder query) {
        final SqlConditionClauseStart conditionClauseStart = new SqlConditionClauseStart(usingTable, null, litebridgeContext);
        final CbSqlConditionClauseTerminal terminal = query.apply(conditionClauseStart);
        node = new ConditionGroupNode(node, logicOperator, CbConditionClauseTerminalInspector.getNode(terminal));
        return this;
    }
}
