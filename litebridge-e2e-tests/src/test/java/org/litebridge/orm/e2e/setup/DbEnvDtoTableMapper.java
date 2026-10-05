package org.litebridge.orm.e2e.setup;

import org.litebridge.orm.LitebridgeCore;

import java.sql.SQLException;

public interface DbEnvDtoTableMapper {

    /**
     * Registers DTO-table mappings for Person and Account
     */
    default void registerPersonAndAccountDtoTableMappings(final LitebridgeCore litebridge) throws SQLException {
        registerPersonDtoTableMapping(litebridge);
        registerAccountDtoTableMapping(litebridge);
    }

    String qualifyName(final String tableName);

    default String transformColumnName(final String columnName) {
        return columnName;
    }

    void registerPersonDtoTableMapping(final LitebridgeCore litebridge);

    void registerAccountDtoTableMapping(final LitebridgeCore litebridge);
}
