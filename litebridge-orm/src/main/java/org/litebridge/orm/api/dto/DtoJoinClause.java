package org.litebridge.orm.api.dto;

import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.condition.AbstractCbConditionClauseTerminal;
import org.litebridge.orm.api.condition.QueryConditionBuilder;
import org.litebridge.orm.api.dto.condition.DtoConditionClauseStart;
import org.litebridge.orm.api.select.impl.AbstractJoinClause;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.ConditionGroupNode;
import org.litebridge.orm.engine.ast.ConditionJoinUsingNode;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.ProtoExpressionSpec;
import org.litebridge.orm.expression.select.SelectFieldSpec;
import org.litebridge.orm.meta.QueryField;
import org.litebridge.orm.meta.QueryFieldInspector;

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
        final DtoConditionClauseStart<DTO> conditionClauseStart = new DtoConditionClauseStart<>(null, litebridgeContext);
        final AbstractCbConditionClauseTerminal<DTO> terminal = builder.apply(conditionClauseStart);
        final QueryNode conditionNode = terminal.node();
        final ConditionGroupNode groupNode = new ConditionGroupNode(null, LogicOperator.NOOP, conditionNode);
        return terminalCreator.apply(groupNode);
    }
}
