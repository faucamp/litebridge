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

public final class SqlMergeOnStep extends MergeOnStep<Row,
        SqlMergeOnConditionClauseTerminal,
        SqlMergeUpdateStep,
        MergeInsertStep,
        SqlQueryConditionBuilder> {

    /**
     * Creates a new {@code SqlMergeOnStep} instance.
     *
     * @param usingTable        the merge using table
     * @param mergeNode         the root merge node
     * @param litebridgeContext Litebridge context
     */
    public SqlMergeOnStep(final String usingTable,
                          final @Nullable String usingAlias,
                          final MergeNode mergeNode,
                          final LitebridgeContext litebridgeContext) {
        super(usingTable, usingAlias, mergeNode, litebridgeContext);
    }

    /**
     * Creates a new {@code SqlMergeOnStep} instance..
     *
     * @param subselectNode     Subquery to merge on.
     * @param mergeNode         the root merge node
     * @param litebridgeContext the Litebridge context
     */
    public SqlMergeOnStep(final QueryNode subselectNode,
                          final @Nullable String usingAlias,
                          final MergeNode mergeNode,
                          final LitebridgeContext litebridgeContext) {
        super(subselectNode, usingAlias, mergeNode, litebridgeContext);
    }

    /**
     * Creates a new {@code SqlMergeOnStep} instance.
     *
     * @param valuesSpec        Values from target specification
     * @param mergeNode         the root merge node
     * @param litebridgeContext the Litebridge context
     */
    protected SqlMergeOnStep(final ValuesSpec valuesSpec,
                             final MergeNode mergeNode,
                             final LitebridgeContext litebridgeContext) {
        super(valuesSpec, mergeNode, litebridgeContext);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public SqlMergeOnConditionClauseTerminal on(final SqlQueryConditionBuilder query) {
        final SqlConditionClauseStart conditionClauseStart = new SqlConditionClauseStart(usingTable, null, litebridgeContext);
        final CbSqlConditionClauseTerminal terminal = query.apply(conditionClauseStart);
        final ConditionGroupNode onConditionNode = new ConditionGroupNode(null, LogicOperator.NOOP, CbConditionClauseTerminalInspector.getNode(terminal));
        return new SqlMergeOnConditionClauseTerminal(usingTable, usingQueryNode, usingValues, usingAlias, onConditionNode, mergeNode, litebridgeContext);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    protected SqlMergeOnConditionClauseTerminal createMergeOnConditionClauseTerminal(final QueryNode conditionNode, final String alias) {
        return new SqlMergeOnConditionClauseTerminal(usingTable, usingQueryNode, usingValues, alias, conditionNode, mergeNode, litebridgeContext);
    }
}
