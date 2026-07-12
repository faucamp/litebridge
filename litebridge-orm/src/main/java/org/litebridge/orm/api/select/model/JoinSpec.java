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
     * Returns the SPI join object.
     *
     * @return the SPI join object.
     */
    Join toJoin();
}
