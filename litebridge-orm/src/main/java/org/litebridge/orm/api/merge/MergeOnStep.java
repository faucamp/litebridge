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

    protected final Table sourceTable;
    protected final Table destinationTable;
    protected final MergeNode mergeNode;
    protected final LitebridgeContext litebridgeContext;

    public MergeOnStep(final MergeNode mergeNode, final Table sourceTable, final LitebridgeContext litebridgeContext) {
        this.sourceTable = sourceTable;
        this.destinationTable = mergeNode.table();
        this.mergeNode = mergeNode;
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

        return new MergeConditionClause<>(litebridgeContext,
                LogicOperator.NOOP,
                expression,
                null,
                conditionNode -> {
                    final UsingNode usingNode = new UsingNode(mergeNode, sourceTable, conditionNode);
                    return new MergeConditionClauseTerminal<>(sourceTable, mergeNode, usingNode, litebridgeContext);
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
