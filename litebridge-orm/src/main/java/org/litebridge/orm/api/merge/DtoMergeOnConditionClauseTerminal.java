package org.litebridge.orm.api.merge;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.condition.CbConditionClauseTerminalInspector;
import org.litebridge.orm.api.condition.CbDtoConditionClauseTerminal;
import org.litebridge.orm.api.condition.DtoConditionClauseStart;
import org.litebridge.orm.api.condition.DtoQueryConditionBuilder;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.ConditionGroupNode;
import org.litebridge.orm.engine.ast.MergeNode;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.select.ValuesSpec;

public final class DtoMergeOnConditionClauseTerminal<DTO>
        extends MergeOnConditionClauseTerminal<DTO,
        DtoMergeOnConditionClauseTerminal<DTO>,
        DtoMergeUpdateStep<DTO>,
        DtoMergeInsertStep,
        DtoQueryConditionBuilder<DTO>> {

    /**
     * Creates a new {@code DtoMergeOnConditionClauseTerminal} instance.
     *
     * @param usingDtoClass     DTO class to use as the merge source.
     * @param usingQueryNode    Subquery to merge on.
     * @param usingValues       Values to merge on.
     * @param usingAlias        Alias to use for the merge source.
     * @param on                The using on condition clause query node.
     * @param mergeNode         The root merge query node.
     * @param litebridgeContext Current Litebridge context.
     */
    public DtoMergeOnConditionClauseTerminal(final Class<?> usingDtoClass, final @Nullable QueryNode usingQueryNode, final @Nullable ValuesSpec usingValues, final @Nullable String usingAlias, final QueryNode on, final MergeNode mergeNode, final LitebridgeContext litebridgeContext) {
        super(null, usingDtoClass, usingQueryNode, usingValues, usingAlias, on, mergeNode, litebridgeContext);
    }

    @Override
    protected DtoMergeOnConditionClauseTerminal<DTO> onImpl(final LogicOperator logicOperator, final DtoQueryConditionBuilder<DTO> query) {
        final DtoConditionClauseStart<DTO> conditionClauseStart = new DtoConditionClauseStart<>(null, litebridgeContext);
        final CbDtoConditionClauseTerminal<DTO> terminal = query.apply(conditionClauseStart);
        node = new ConditionGroupNode(node, logicOperator, CbConditionClauseTerminalInspector.getNode(terminal));
        return this;
    }
}
