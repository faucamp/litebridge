package org.litebridge.orm.api.select.impl;

import org.jspecify.annotations.Nullable;
import org.litebridge.orm.api.select.GroupByClauseTerminal;
import org.litebridge.orm.api.select.HavingConditionClause;
import org.litebridge.orm.api.select.HavingConditionClauseTerminal;
import org.litebridge.orm.api.select.OrderByClause;
import org.litebridge.orm.api.select.OrderByClauseChain;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.api.select.model.SelectSpec;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.SelectEngineTerminal;
import org.litebridge.orm.expression.ExpressionSpec;

public abstract class AbstractGroupByClauseTerminal<DTO,
        HCC extends HavingConditionClause<DTO, HCC, HCCT, OBC, OBCC>,
        HCCT extends HavingConditionClauseTerminal<DTO, HCC, HCCT, OBC, OBCC>,
        OBC extends OrderByClause<DTO, OBC, OBCC>,
        OBCC extends OrderByClauseChain<DTO, OBC, OBCC>,
        SSP extends SelectSpec>

        extends OrderByClauseTerminalImpl<DTO>
        implements GroupByClauseTerminal<DTO, HCC, HCCT, OBC, OBCC> {

    protected final String @Nullable [] columns;
    protected final ExpressionSpec @Nullable [] expressions;

    protected AbstractGroupByClauseTerminal(final String @Nullable [] columns,
                                            final QueryNode node,
                                            final SelectEngineTerminal selectEngineTerminal,
                                            final LitebridgeContext litebridgeContext) {
        this(columns, null, node, selectEngineTerminal, litebridgeContext);
    }

    protected AbstractGroupByClauseTerminal(final ExpressionSpec @Nullable [] expressions,
                                            final QueryNode node,
                                            final SelectEngineTerminal selectEngineTerminal,
                                            final LitebridgeContext litebridgeContext) {
        this(null, expressions, node, selectEngineTerminal, litebridgeContext);
    }

    private AbstractGroupByClauseTerminal(final String @Nullable [] columns,
                                          final ExpressionSpec @Nullable [] expressions,
                                          final QueryNode node,
                                          final SelectEngineTerminal selectEngineTerminal,
                                          final LitebridgeContext litebridgeContext) {
        super(node, selectEngineTerminal, litebridgeContext);
        this.columns = columns;
        this.expressions = expressions;
    }
}
