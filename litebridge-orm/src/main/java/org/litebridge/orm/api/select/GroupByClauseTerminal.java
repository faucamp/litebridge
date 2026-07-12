package org.litebridge.orm.api.select;

import org.litebridge.orm.expression.ExpressionSpec;

/**
 * Terminal clause in a fluent API for constructing SQL GROUP BY statements.
 *
 * @param <DTO>  the data transfer object (DTO) type that represents the result of the query
 * @param <HCC>  the type of the HAVING condition clause for further filtering
 * @param <HCCT> the terminal type of the HAVING condition clause, marking the end of HAVING conditions
 * @param <OBC>  the type of the ORDER BY clause defining result sorting
 * @param <OBCC> the type of the ORDER BY clause chain for chaining multiple sorting expressions
 */
public interface GroupByClauseTerminal<DTO,
        HCC extends HavingConditionClause<DTO, HCC, HCCT, OBC, OBCC>,
        HCCT extends HavingConditionClauseTerminal<DTO, HCC, HCCT, OBC, OBCC>,
        OBC extends OrderByClause<DTO, OBC, OBCC>,
        OBCC extends OrderByClauseChain<DTO, OBC, OBCC>>

        extends HavingClauseTerminal<DTO, OBC, OBCC> {

    /**
     * Starts a HAVING clause for the SQL query.
     *
     * @param expression the expression to apply the filtering condition on
     * @return an instance of {@code HCC} representing the next stage of the having condition clause
     */
    HCC having(ExpressionSpec expression);

}
