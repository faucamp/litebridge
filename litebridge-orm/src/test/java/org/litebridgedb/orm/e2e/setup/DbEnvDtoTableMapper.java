package org.litebridgedb.orm.e2e.setup;

import org.litebridgedb.orm.Litebridge;

import java.sql.SQLException;

public interface DbEnvDtoTableMapper {

    /**
     * Registers DTO-table mappings for Person and Account
     */
    default void registerPersonAndAccountDtoTableMappings(final Litebridge litebridge, final boolean typeSafe) throws SQLException {
        registerPersonDtoTableMapping(litebridge, typeSafe);
        registerAccountDtoTableMapping(litebridge, typeSafe);
    }

    String qualifyName(final String tableName);

    default String transformColumnName(final String columnName) {
        return columnName;
    }

    void registerPersonDtoTableMapping(final Litebridge litebridge, final boolean typeSafe);

    void registerAccountDtoTableMapping(final Litebridge litebridge, final boolean typeSafe);
}
