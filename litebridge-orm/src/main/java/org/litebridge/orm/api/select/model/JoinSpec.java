package org.litebridge.orm.api.select.model;

import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.query.Join;

/**
 * Specification for a database "JOIN" clause.
 */
public interface JoinSpec {
    /**
     * Returns the table to join.
     *
     * @return the table to join.
     */
    Table table();

    /**
     * Join using a specific column.
     *
     * @param column the column name
     * @return the condition specification
     */
    ConditionSpec using(String column);

    /**
     * Returns the current condition group specification.
     *
     * @return the current condition group specification
     */
    ConditionGroupSpec currentConditionGroupSpec();

    /**
     * Returns the SPI join object.
     *
     * @return the SPI join object.
     */
    Join toJoin();
}
