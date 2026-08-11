package org.litebridge.orm.api.merge;

import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.dto.delete.DtoDeleteWhereConditionClause;
import org.litebridge.orm.api.select.ast.MergeNode;
import org.litebridge.orm.api.select.ast.UsingNode;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;

import java.util.function.Supplier;

public sealed class MergeOnStep<DTO, MUS extends MergeUpdateStep<DTO>> permits DtoMergeOnStep {

    protected final Table destinationTable;
    protected final UsingNode node;
    protected final LitebridgeContext litebridgeContext;

    public MergeOnStep(final MergeNode mergeNode, final Table sourceTable, final LitebridgeContext litebridgeContext) {
        this.destinationTable = mergeNode.table();
        this.node = new UsingNode(mergeNode, sourceTable);
        this.litebridgeContext = litebridgeContext;
    }

    public MergeConditionClause<DTO, MUS> on(final String column) {
        final Column spiColumn = litebridgeContext.tableMetaDataCache()
                .ensureTableMetaData(destinationTable)
                .column(column)
                .toColumn();
        return on(new SelectColumnSpec(spiColumn));
    }

    public MergeConditionClause<DTO, MUS> on(final ExpressionSpec expression) {
        final Supplier<MUS> mergeUpdateStepSupplier;

        if (this instanceof DtoMergeOnStep<?> dtoDtoMergeOnStep) {
            //TODO: sort out query node
            mergeUpdateStepSupplier = () -> (MUS) new DtoMergeUpdateStep<>(dtoDtoMergeOnStep.dtoClass(), destinationTable, null, litebridgeContext);
        } else {
            //TODO: sort out query node
            mergeUpdateStepSupplier = () -> (MUS) new SqlMergeUpdateStep(destinationTable, null, litebridgeContext);
        }

        return new MergeConditionClause<>(litebridgeContext,
                LogicOperator.NOOP,
                expression,
                conditionNode -> new MergeConditionClauseTerminal<>(node.table(), conditionNode, mergeUpdateStepSupplier, litebridgeContext));
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
