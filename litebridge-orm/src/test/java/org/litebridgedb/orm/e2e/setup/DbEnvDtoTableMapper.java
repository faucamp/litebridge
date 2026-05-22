package org.litebridgedb.orm.e2e.setup;

import org.litebridgedb.orm.Litebridge;

import java.sql.SQLException;

public interface DbEnvDtoTableMapper {

    /**
     * Registers DTO-table mappings for Person and Account
     */
    default void registerPersonAndAccountDtoTableMappings(final Litebridge litebridge) throws SQLException {
        registerPersonDtoTableMapping(litebridge);
        registerAccountDtoTableMapping(litebridge);
    }

    String qualifyName(final String tableName);

    void registerPersonDtoTableMapping(final Litebridge litebridge);

    void registerAccountDtoTableMapping(final Litebridge litebridge);
}
