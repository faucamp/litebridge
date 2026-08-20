package org.litebridge.orm.api.merge;

import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.dto.delete.DtoDeleteWhereConditionClause;
import org.litebridge.orm.api.select.ConditionClauseTerminal;
import org.litebridge.orm.api.select.ast.MergeNode;
import org.litebridge.orm.api.select.ast.UsingNode;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;

public sealed class MergeOnStep<DTO, MUS extends MergeUpdateStep<DTO>>
        extends MergeStepBase
        permits DtoMergeOnStep {

    protected final MergeNode mergeNode;
    protected final LitebridgeContext litebridgeContext;

    public MergeOnStep(final MergeNode mergeNode, final Table usingTable, final LitebridgeContext litebridgeContext) {
        super(mergeNode.table(), usingTable, litebridgeContext);
        this.mergeNode = mergeNode;
        this.litebridgeContext = litebridgeContext;
    }

    public MergeConditionClause<DTO, MUS, MergeOnConditionClauseTerminal<DTO, MUS>> on(final String column) {
        final Column spiColumn = createSpiColumn(column);
        return on(new SelectColumnSpec(spiColumn));
    }

    public MergeConditionClause<DTO, MUS, MergeOnConditionClauseTerminal<DTO, MUS>> on(final ExpressionSpec expression) {
        return new MergeConditionClause<>(litebridgeContext,
                LogicOperator.NOOP,
                expression,
                null,
                conditionNode -> {
                    final UsingNode usingNode = new UsingNode(mergeNode, usingTable, conditionNode);
                    return new MergeOnConditionClauseTerminal<>(mergeNode, usingNode, litebridgeContext);
                });
    }

    DtoDeleteWhereConditionClause<DTO> whereImpl(final LogicOperator logicOperator, final String field) {
//        final Column column = deleteSpec.dtoTable().getColumnForFieldName(field).toColumn();
//        return whereImpl(logicOperator, new SelectColumnSpec(column));
        throw new UnsupportedOperationException("Not implemented yet");
    }

    DtoDeleteWhereConditionClause<DTO> whereImpl(final LogicOperator logicOperator, final ExpressionSpec expression) {
//        final Function<QueryNode, DtoDeleteWhereConditionClauseTerminal<DTO>> recreator = n -> {
//            this.node = new WhereNode(this.node, n);
//            return new DtoDeleteWhereConditionClauseTerminalImpl<>(this);
//        };
//        return new DtoDeleteWhereConditionClause<>(litebridgeContext, logicOperator, expression, recreator);
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
