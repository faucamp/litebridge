package org.litebridge.orm.api.dto.condition;

import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.condition.AbstractCbConditionClause;
import org.litebridge.orm.api.condition.AbstractConditionClauseStart;
import org.litebridge.orm.api.select.ast.ConditionContext;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.model.ConditionSpec;
import org.litebridge.orm.engine.FromClauseEngine;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;
import org.litebridge.orm.persistence.OrmTable;

/**
 * Start of a DTO-based condition clause.
 *
 * @param <DTO> the type of the DTO
 */
public class DtoConditionClauseStart<DTO> extends AbstractConditionClauseStart<DTO> {

    private final OrmTable ormTable;
    private final QueryNode node;

    /**
     * Creates a new DTO condition clause start.
     *
     * @param ormTable         the ORM table metadata
     * @param fromClauseEngine the from clause engine
     */
    public DtoConditionClauseStart(final OrmTable ormTable,
                                   final FromClauseEngine fromClauseEngine,
                                   final QueryNode node) {
        super(fromClauseEngine);
        this.ormTable = ormTable;
        this.node = node;
    }

    @Override
    public CbDtoConditionClause<DTO> where(final String field) {
        final Column column = ormTable.getColumnForFieldName(field).toColumn();
        return (CbDtoConditionClause<DTO>) where(new SelectColumnSpec(column));
    }

    @Override
    public AbstractCbConditionClause<DTO> where(final ExpressionSpec expression) {
        //TODO: check logical operator and condition context, and node
        return new CbDtoConditionClause<>(ormTable, fromClauseEngine, LogicOperator.NOOP, expression, ConditionContext.WHERE, node, node -> new CbDtoConditionClauseTerminal<>(ormTable, fromClauseEngine, node));
    }
}
