package org.litebridgedb.orm.api.register;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.db.spi.DatabaseProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Registration context for associating a Data Transfer Object (DTO) class with database-related functionality
 * and specifications.
 * <p>
 * This class is the first step to configure mappings between a DTO and database tables,
 * and to register additional interfaces that the DTO may implement.
 * <p>
 * It serves as a builder-like structure for progressively defining the registration configuration.
 */
public final class RegistrationContext {

    private final Class<?> dtoClass;
    private final DatabaseProvider databaseProvider;
    private @Nullable List<Class<?>> dtoInterfaces;

    public RegistrationContext(final Class<?> dtoClass, final DatabaseProvider databaseProvider) {
        this.dtoClass = dtoClass;
        this.databaseProvider = databaseProvider;
    }

    /**
     * Specifies an additional superinterface of the DTO class that will be recognised by Litebridge relational mapping
     * if used in collections.
     * <p>
     * For example, if the DTO class is defined as {@code class MyDto implements MyInterface},
     * then {@code allowInterface(MyInterface.class)} should be called to ensure that Litebridge can correctly handle
     * collections of {@code MyInterface} instances in related DTOs.
     *
     * @param dtoInterface The additional superinterface to allow
     * @return This registration context for method chaining
     */
    public RegistrationContext allowInterface(final Class<?> dtoInterface) {
        if (dtoInterfaces == null) {
            dtoInterfaces = new ArrayList<>();
        }

        dtoInterfaces.add(dtoInterface);
        return this;
    }

    /**
     * Maps a Data Transfer Object (DTO) class to a specified database table.
     * This method initiates the final stage of the registration process by associating
     * the DTO class with the given table name and any provided DTO interfaces.
     * <p>
     * Following this, the table mapping is complete and the builder proceeds to field/lhs mappings.
     *
     * @param tableName The name of the table in the database to which the DTO class should be mapped.
     * @return A {@code RegistrationContextTerminal} instance to finalize the mapping configuration.
     */
    public RegistrationContextTerminal mapToTable(final String tableName) {
        return new RegistrationContextTerminal(dtoClass, tableName, databaseProvider, dtoInterfaces);
    }
}
