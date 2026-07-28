package org.litebridge.orm.api.dto.condition;

import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.condition.AbstractCbConditionClause;
import org.litebridge.orm.api.condition.AbstractCbConditionClauseTerminal;
import org.litebridge.orm.api.condition.AbstractConditionClauseStart;
import org.litebridge.orm.api.condition.QueryConditionBuilder;
import org.litebridge.orm.api.dto.DtoSelector;
import org.litebridge.orm.api.dto.DtoWhereConditionClause;
import org.litebridge.orm.api.dto.DtoWhereConditionClauseTerminal;
import org.litebridge.orm.api.select.ast.ConditionContext;
import org.litebridge.orm.api.select.ast.ConditionGroupNode;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.ast.WhereNode;
import org.litebridge.orm.api.select.model.ConditionGroupSpec;
import org.litebridge.orm.api.select.model.ConditionSpec;
import org.litebridge.orm.engine.FromClauseEngine;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;
import org.litebridge.orm.persistence.OrmTable;

/**
 * Implementation of a terminal condition clause for DTO-based queries.
 *
 * @param <DTO> The type of the DTO being queried.
 */
public final class CbDtoConditionClauseTerminal<DTO> extends AbstractCbConditionClauseTerminal<DTO> {

    private final OrmTable ormTable;

    /**
     * Constructs a new {@code CbDtoConditionClauseTerminal}.
     *
     * @param ormTable           The ORM table metadata.
     * @param fromClauseEngine   The FROM clause engine.
     */
    public CbDtoConditionClauseTerminal(final OrmTable ormTable, final FromClauseEngine fromClauseEngine, final QueryNode node) {
        super(fromClauseEngine, node);
        this.ormTable = ormTable;
    }


    @Override
    protected CbDtoConditionClause<DTO> whereImpl(final LogicOperator logicOperator, final String field) {
        final Column column = ormTable.getColumnForFieldName(field).toColumn();
        return whereImpl(logicOperator, new SelectColumnSpec(column));
    }

    @Override
    protected CbDtoConditionClause<DTO> whereImpl(final LogicOperator logicOperator, final ExpressionSpec expression) {
        return new CbDtoConditionClause<>(ormTable,
                fromClauseEngine,
                logicOperator,
                expression,
                ConditionContext.WHERE,
                node,
                conditionNode -> new CbDtoConditionClauseTerminal<>(ormTable, fromClauseEngine, new ConditionGroupNode(node, logicOperator, conditionNode)));
    }

    @Override
    protected AbstractCbConditionClause<DTO> createCbConditionClause(final ConditionSpec conditionSpec) {
//        return new CbDtoConditionClause<>(conditionSpec, conditionGroupSpec, ormTable, fromClauseEngine);
        //TODO: reimplement
        throw new UnsupportedOperationException("Need to reimplement");
    }

    @Override
    protected AbstractConditionClauseStart<DTO> createConditionClauseStart(final ConditionGroupSpec subgroup) {
        //return new DtoConditionClauseStart<>(subgroup, ormTable, fromClauseEngine);
        //TODO: reimplement
        throw new UnsupportedOperationException("Need to reimplement");
    }

    @Override
    protected AbstractCbConditionClauseTerminal<DTO> whereImpl(final LogicOperator logicOperator, final QueryConditionBuilder<DTO> query) {
//        if (!(node instanceof WhereNode whereNode)) {
//            throw new IllegalArgumentException("AST error: Expected a WhereNode but got " + node);
//        }

        final DtoConditionClauseStart<DTO> conditionClauseStart = new DtoConditionClauseStart<>(ormTable, fromClauseEngine, null);
        AbstractCbConditionClauseTerminal terminal = query.apply(conditionClauseStart);
        this.node = new ConditionGroupNode(node, logicOperator, terminal.node());
        return this;
    }
}
