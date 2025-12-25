package org.litebridge.orm.api.select.impl;

import org.litebridge.orm.api.select.JoinConditionClause;
import org.litebridge.orm.api.select.JoinConditionClauseTerminal;
import org.litebridge.orm.api.select.model.ConditionSpec;

public class JoinConditionClauseImpl<DTO, JCCT extends JoinConditionClauseTerminal<DTO, JoinConditionClauseImpl<DTO, JCCT>, JCCT>>
        extends ConditionClauseImpl<DTO, JoinConditionClauseImpl<DTO, JCCT>, JCCT>
        implements JoinConditionClause<DTO, JoinConditionClauseImpl<DTO, JCCT>, JCCT> {

    public JoinConditionClauseImpl(final ConditionSpec condition, final JCCT conditionTerminal) {
        super(condition, conditionTerminal);
    }
}
