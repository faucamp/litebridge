package org.litebridge.orm.persistence.alias;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Table;

/**
 * Interface for generating aliases for tables and columns.
 */
public interface AliasGenerator {

    @Nullable String columnAlias(Column column);

    @Nullable String tableAlias(Table table);

    String newTableAlias(Table table);

    String newColumnAlias(Column column);

    void setColumnAlias(Column column, String alias);

    String newAlias(String name);

    void pushScope();

    void popScope();

    /**
     * Clears internal alias maps and usage counts.
     */
    void clear();
}
