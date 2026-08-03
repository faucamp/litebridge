package org.litebridge.orm.api.select.model;

import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.query.Join;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.orm.persistence.TableMetaDataCache;

import java.util.Collection;
import java.util.List;

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
     * Pushes a new condition group specification.
     *
     * @param logicOperator the logic operator for the group
     * @return the new condition group specification
     */
    ConditionGroupSpec pushConditionGroupSpec(LogicOperator logicOperator);

    /**
     * Pops the current condition group specification.
     */
    void popConditionGroupSpec();

    /**
     * Returns the SPI join object.
     *
     * @param availableTables the collection of tables currently available in the query context for alias resolution
     * @param bindValues
     * @return the SPI join object.
     */
    Join toJoin(Collection<Table> availableTables, final List<BindValue> bindValues, final TableMetaDataCache tableMetaDataCache, final TypeConverter typeConverter);
}
