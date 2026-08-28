package org.litebridge.orm.api.dto;

import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.orm.api.condition.QueryConditionBuilder;
import org.litebridge.orm.api.select.ast.ConditionJoinUsingNode;
import org.litebridge.orm.api.select.ast.ConditionNode;
import org.litebridge.orm.api.select.ast.JoinNode;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.impl.AbstractJoinClause;
import org.litebridge.orm.api.sql.SqlJoinConditionClause;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.ProtoExpressionSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;
import org.litebridge.orm.expression.select.SelectFieldSpec;
import org.litebridge.orm.meta.QueryField;
import org.litebridge.orm.meta.QueryFieldInspector;
import org.litebridge.orm.persistence.MappedManyToMany;
import org.litebridge.orm.persistence.MappedOneToMany;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.tracking.ClassFieldAccessorCache;
import org.litebridge.tracking.FieldAccessor;

import java.util.List;
import java.util.function.Function;

/**
 * Represents a JOIN clause in a DTO-based query.
 *
 * @param <DTO> the type of the DTO being queried
 */
public final class DtoJoinClause<DTO> extends AbstractJoinClause<DTO,
        DtoJoinConditionClause<DTO>,
        DtoJoinConditionClauseTerminal<DTO>> {

    private final Function<QueryNode, DtoJoinConditionClauseTerminal<DTO>> terminalCreator;

    /**
     * Creates a new instance of {@code DtoJoinClause}.
     *
     * @param terminalCreator the function to create the terminal clause
     */
    public DtoJoinClause(final QueryNode node,
                         final LitebridgeContext litebridgeContext,
                         final Function<QueryNode, DtoJoinConditionClauseTerminal<DTO>> terminalCreator) {
        super(node, litebridgeContext);
        this.terminalCreator = terminalCreator;
    }

    /**
     * Adds a join ON condition to the current join clause based on the specified field.
     * The join condition constrains the relationship between the tables being joined.
     *
     * @param field the name of the field to be used in the join condition
     * @return an instance of the join condition clause to allow further configuration
     */
    public DtoJoinConditionClauseTerminal<DTO> on(final String field) {
        // Try to find the field in the source table first (standard forward join or 1:N from source)
//        final FieldAccessor sourceFieldAccessor = classFieldAccessorCache.fieldAccessorOrNull(table.dtoClass(), field);
//
//        if (sourceFieldAccessor != null) {
//            return table.getOneToManyMappingForField(sourceFieldAccessor)
//                    .map(m -> inverseJoin(m, field))
//                    .orElseGet(() -> table.getManyToManyMappingForField(sourceFieldAccessor)
//                            .map(m -> manyToManyJoin(m, field))
//                            .orElseGet(() -> joinOnForward(field, field))
//                    );
//        }
//
//        // Field not in source; try target table
//        final FieldAccessor targetFieldAccessor = classFieldAccessorCache.fieldAccessorOrThrow(targetTable.dtoClass(), field);
//        return joinOnInverse(targetFieldAccessor.name(), field);
        final ConditionJoinUsingNode conditionJoinUsingNode = new ConditionJoinUsingNode(null, LogicOperator.NOOP, field, null);
        return terminalCreator.apply(conditionJoinUsingNode);
    }

    /**
     * Adds a join ON condition based on a query expression.
     *
     * @param expression the expression to use for the join condition
     * @return an instance of the join condition clause to allow further configuration
     */
    public DtoJoinConditionClauseTerminal<DTO> on(final ExpressionSpec expression) {
        return switch (expression) {
            case QueryField queryField -> on(QueryFieldInspector.getFieldName(queryField));
            case ProtoExpressionSpec protoExpressionSpec -> on(protoExpressionSpec.column());
            case SelectFieldSpec selectFieldSpec -> on(selectFieldSpec.field().name());
            default -> throw new IllegalArgumentException("Unsupported JOIN ON expression: " + expression);
        };
    }

    /**
     * Adds a join ON condition based on a query condition builder.
     *
     * @param builder the builder for the join condition
     * @return an instance of the join condition clause to allow further configuration
     */
    public DtoJoinConditionClauseTerminal<DTO> on(final QueryConditionBuilder<DTO> builder) {
//        final DtoConditionClauseStart<DTO> conditionClauseStart = new DtoConditionClauseStart<>(targetTable, delegate.litebridgeContext().fromClauseEngine(), null);
//        final AbstractCbConditionClauseTerminal<DTO> terminal = builder.apply(conditionClauseStart);
//        final QueryNode conditionNode = terminal.node();
//
//        final ConditionGroupNode groupNode = new ConditionGroupNode(null, LogicOperator.NOOP, conditionNode);
//        return terminalCreator.apply(groupNode);
        throw new UnsupportedOperationException("Not implemented yet");
    }

    private DtoJoinConditionClauseTerminal<DTO> joinOnForward(final String lookupField, final String relationshipField) {
//        final ColumnMetaData sourceColumnMetaData = table.getColumnForFieldName(lookupField);
//
//        if (sourceColumnMetaData.getJoinColumn() == null) {
//            throw new IllegalStateException("No join column specified for column '%s' mapped to field '%s' in table '%s'".formatted(sourceColumnMetaData.name(), lookupField, table.getMetaData().name()));
//        }
//
//        final Column sourceColumn = sourceColumnMetaData.toColumn();
//        final ColumnMetaData targetColumnMetaData = targetTable.getColumnMetaData(sourceColumnMetaData.getJoinColumn());
//        final Column targetColumn = targetColumnMetaData.toColumn();
//
//        final ConditionNode conditionNode;
//        if (sourceColumnMetaData.name().equals(targetColumnMetaData.name())) {
//            conditionNode = new ConditionNode(null, LogicOperator.NOOP, null, new SelectColumnSpec(sourceColumn), Operator.USING, targetColumnMetaData.name(), relationshipField);
//        } else {
//            conditionNode = new ConditionNode(null, LogicOperator.NOOP, null, new SelectColumnSpec(sourceColumn), Operator.EQ, targetColumn, relationshipField);
//        }
//
//        return terminalCreator.apply(conditionNode);
        throw new UnsupportedOperationException("Not implemented yet");
    }

    private DtoJoinConditionClauseTerminal<DTO> joinOnInverse(final String lookupField, final String relationshipField) {
//        final ColumnMetaData targetColumnMetaData = targetTable.getColumnForFieldName(lookupField);
//
//        if (targetColumnMetaData.getJoinColumn() == null) {
//            throw new IllegalStateException("No join column specified for column '%s' mapped to field '%s' in joined table '%s'".formatted(targetColumnMetaData.name(), lookupField, targetTable.getMetaData().name()));
//        }
//
//        final Column targetColumn = targetColumnMetaData.toColumn();
//        final ColumnMetaData sourceColumnMetaData = table.getColumnMetaData(targetColumnMetaData.getJoinColumn());
//        final Column sourceColumn = sourceColumnMetaData.toColumn();
//
//        final ConditionNode conditionNode;
//        if (targetColumnMetaData.name().equals(sourceColumnMetaData.name())) {
//            conditionNode = new ConditionNode(null, LogicOperator.NOOP, null, new SelectColumnSpec(sourceColumn), Operator.USING, sourceColumnMetaData.name(), relationshipField);
//        } else {
//            conditionNode = new ConditionNode(null, LogicOperator.NOOP, null, new SelectColumnSpec(sourceColumn), Operator.EQ, targetColumn, relationshipField);
//        }
//
//        return terminalCreator.apply(conditionNode);
        throw new UnsupportedOperationException("Not implemented yet");
    }

    private DtoJoinConditionClauseTerminal<DTO> inverseJoin(MappedOneToMany mappedOneToMany, String relationshipField) {
        final FieldAccessor mappedByField = mappedOneToMany.mappedByField();
        return joinOnInverse(mappedByField.name(), relationshipField);
    }

    private DtoJoinConditionClauseTerminal<DTO> manyToManyJoin(MappedManyToMany mappedManyToMany, String relationshipField) {
//        final OrmTable joinOrmTable = mappedManyToMany.joinTable();
//
//        // Create intermediate JoinNode (raw join on the join table)
//        final JoinNode intermediateJoinNode = new JoinNode(
//                delegate.node(),
//                "INNER",
//                joinOrmTable.dtoClass(),
//                table.dtoClass(),
//                joinOrmTable.getMetaData().name()
//        );
//
//        // Set ON condition for intermediate join: sourceTable.pk == joinTable.joinColumn
//        final List<ColumnMetaData> sourcePkColumns = table.getMetaData().primaryKey();
//        if (sourcePkColumns.size() != 1) {
//            throw new UnsupportedOperationException("Many-to-many joins currently only support single-column primary keys");
//        }
//        final Column sourcePkColumn = sourcePkColumns.getFirst().toColumn();
//        final Column joinTableJoinColumn = joinOrmTable.getColumnMetaData(mappedManyToMany.joinColumn()).toColumn();
//
//        final ConditionNode intermediateCondition = new ConditionNode(null, LogicOperator.NOOP, null, new SelectColumnSpec(sourcePkColumn), Operator.EQ, joinTableJoinColumn);
//        intermediateJoinNode.withCondition(intermediateCondition);
//
//        // Create target JoinNode (standard join on the target DTO)
//        // Note: we set sourceDtoClass to the original table (not the proxy) so QueryCompiler/SelectSpecDtoMapper can find the collection field
//        final JoinNode targetJoinNode = new JoinNode(
//                intermediateJoinNode,
//                "INNER",
//                targetTable.dtoClass(),
//                table.dtoClass(),
//                null
//        );
//
//        // Set ON condition for target join: joinTable.inverseJoinColumn == targetTable.pk
//        final List<ColumnMetaData> targetPkColumns = targetTable.getMetaData().primaryKey();
//
//        if (targetPkColumns.size() != 1) {
//            throw new UnsupportedOperationException("Many-to-many joins currently only support single-column primary keys");
//        }
//
//        final Column targetPkColumn = targetPkColumns.getFirst().toColumn();
//        final Column joinTableInverseJoinColumn = joinOrmTable.getColumnMetaData(mappedManyToMany.inverseJoinColumn()).toColumn();
//
//        final ConditionNode targetCondition = new ConditionNode(null, LogicOperator.NOOP, null, new SelectColumnSpec(joinTableInverseJoinColumn), Operator.EQ, targetPkColumn, relationshipField);
//        targetJoinNode.withCondition(targetCondition);
//
//        // Update delegate with the chain of nodes
//        delegate.withNode(targetJoinNode);
//
//        return new DtoJoinConditionClauseTerminal<>(targetJoinNode, (DtoSelector<DTO>) delegate);
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
