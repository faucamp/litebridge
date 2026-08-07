package org.litebridge.orm.api.select;

/**
 * Generic join condition clause for constructing SQL-like join conditions in a type-safe
 * and fluent manner.
 * <p>
 * This interface builds upon {@link ConditionClause}, providing the structure for
 * defining conditional expressions specifically for join operations.
 *
 * @param <DTO>  the type of the data transfer object associated with the query
 * @param <SELF> the type of the implementing subclass to enable type-safe chaining
 * @param <JCCT> the type of the terminal join condition clause used for finalizing join conditions
 */
public interface JoinConditionClause<DTO,
        SELF extends JoinConditionClause<DTO, SELF, JCCT>,
        JCCT extends JoinConditionClauseTerminal<DTO, SELF, JCCT>>

        extends ConditionClause<DTO, SELF, JCCT> {

    /**
     * Join using a specific column.
     *
     * @param column the column name
     * @return the join condition clause terminal
     */
    JCCT using(String column);
}
