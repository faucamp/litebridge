package org.litebridge.orm;

import org.litebridge.commons.ClassUtils;
import org.litebridge.commons.CollectionUtils;
import org.litebridge.commons.ObjectUtils;
import org.litebridge.commons.StringUtils;
import org.litebridge.db.spi.Aliased;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.orm.api.dto.DtoFromClauseTerminal;
import org.litebridge.orm.api.dto.DtoSelector;
import org.litebridge.orm.api.spec.ColumnSpec;
import org.litebridge.orm.api.spec.FieldSpec;
import org.litebridge.orm.api.spec.TableSpec;
import org.litebridge.orm.api.sql.SqlFromClause;
import org.litebridge.orm.api.sql.SqlSelector;
import org.litebridge.orm.persistence.DefaultDtoMapper;
import org.litebridge.orm.persistence.DtoAliasRegistry;
import org.litebridge.orm.persistence.DtoIntrospector;
import org.litebridge.orm.persistence.DtoMapper;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.orm.persistence.PersistenceFacade;
import org.litebridge.orm.persistence.TableRegistry;
import org.litebridge.tracking.ChangeTracker;
import org.litebridge.tracking.FieldAccessor;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Primary entry point for Litebridge.
 * <p>
 * Litebridge is responsible for managing database interactions,
 * including mapping Data Transfer Objects (DTOs) to tables,
 * registering tables, change tracking, and executing query operations.
 * <p>
 * It provides a mechanism to translate between DTOs and database tables,
 * facilitating CRUD operations while maintaining consistency and integrity.
 * <p>
 * Litebridge ensures thread safety by using immutable internal structures and
 * leveraging the {@code DatabaseProvider} and {@code PersistenceFacade} for
 * database interactions, ensuring that operations are performed safely and
 * efficiently.
 */
public class Litebridge {

    private static final Aliased[] ALL_COLUMNS = new Aliased[0];

    private final TableRegistry tableRegistry = new TableRegistry();
    private final ChangeTracker changeTracker = new ChangeTracker();
    private final DatabaseProvider databaseProvider;
    private final PersistenceFacade persistenceFacade;

    /**
     * Constructs a Litebridge instance with the specified database provider.
     *
     * @param databaseProvider the database provider to be used by Litebridge.
     *                         This parameter is required to set up database operations
     *                         and facilitate persistence functionalities. Must not be null.
     */
    public Litebridge(final DatabaseProvider databaseProvider) {
        this.databaseProvider = databaseProvider;
        this.persistenceFacade = new PersistenceFacade(tableRegistry, databaseProvider);
    }

    /**
     * Register a Data Transfer Object (DTO) class with its corresponding table specification.
     * This method maps the DTO class to a database table and stores the association
     * in the table registry to enable database operations such as insert, update, or query.
     *
     * @param dtoClass  the class of the Data Transfer Object to be registered; must not be null.
     * @param tableSpec the table specification defining the mapping of the DTO class to the database table; must not be null.
     * @throws SQLException if an error occurs during the mapping or registration process.
     */
    public void register(final Class<?> dtoClass, final TableSpec tableSpec) throws SQLException {
        tableRegistry.addTable(dtoClass, mapToTable(dtoClass, tableSpec));
    }

    /**
     * Initiate change tracking for the given Data Transfer Object (DTO).
     * <p>
     * This process involves associating the DTO with its corresponding ORM table
     * and enabling change tracking for the object's fields.
     *
     * @param <T> the type of the Data Transfer Object being tracked.
     * @param dto the Data Transfer Object to be tracked; must not be null and
     *            must be registered with a corresponding table.
     * @return the tracked Data Transfer Object.
     * @throws IllegalArgumentException if the DTO is null or if its class is not registered.
     */
    public <T> T track(final T dto) {
        if (dto == null) {
            throw new IllegalArgumentException("DTO cannot be null");
        }

        final OrmTable table = tableRegistry.getTable(dto.getClass());

        if (table == null) {
            throw new IllegalArgumentException("DTO class not registered: '%s'".formatted(dto.getClass().getName()));
        }

        table.trackDto(dto);
        return dto;
    }

    /**
     * Save the given Data Transfer Object (DTO) to the database, via a SQL INSERT or UPDATE statement.
     * <p>
     * This method utilises the persistence facade to perform the save operation. It handles SQL exceptions
     * and ensures the integrity of the save process.
     *
     * @param dto the Data Transfer Object to be saved in the database. It must
     *            be a valid and properly configured DTO.
     * @throws IllegalStateException if an error occurs during the save operation.
     */
    public void save(final Object dto) {
        try {
            persistenceFacade.save(dto);
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to save DTO: " + dto, ex);
        }
    }

    /**
     * Insert the specified Data Transfer Object (DTO) into the database via a SQL INSERT statement.
     * <p>
     * This method uses the persistence facade to perform the insertion
     * and handles any SQL exceptions that might occur during the process.
     *
     * @param dto the Data Transfer Object to be inserted into the database.
     *            It must correspond to a properly configured and valid DTO.
     * @throws IllegalStateException if an error occurs during the insertion process.
     */
    public void insert(final Object dto) {
        try {
            persistenceFacade.insert(dto);
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to insert DTO: " + dto, ex);
        }
    }

    /**
     * Update the specified Data Transfer Object (DTO) in the database via a SQL UPDATE statement.
     * <p>
     * This method utilises the persistence facade to perform the update operation
     * and handles any SQL exceptions that might occur during the process.
     *
     * @param dto the Data Transfer Object to be updated in the database.
     *            It must represent a valid and properly tracked DTO.
     * @throws IllegalStateException if an error occurs while performing the update.
     */
    public void update(final Object dto) {
        try {
            persistenceFacade.update(dto);
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to update DTO: " + dto, ex);
        }
    }

    /**
     * Select a registered Data Transfer Object (DTO) type for database query operations.
     *
     * @param <DTO>    The type of the DTO to select.
     * @param dtoClass The class of the DTO to be queried, which must already be registered.
     * @return A {@link DtoSelector} instance for querying and retrieving data for the specified DTO class.
     * @throws IllegalArgumentException if the specified DTO class is not registered in the table registry.
     */
    public <DTO> DtoFromClauseTerminal<DTO> select(final Class<DTO> dtoClass) {
        final OrmTable table = tableRegistry.getTableOrThrow(dtoClass);
        final DtoAliasRegistry dtoAliasRegistry = new DtoAliasRegistry();
        final DtoMapper dtoMapper = new DefaultDtoMapper(tableRegistry, databaseProvider.getTypeConverter(), dtoAliasRegistry);
        return new DtoSelector<>(dtoClass, table, tableRegistry, databaseProvider, dtoMapper, dtoAliasRegistry).select();
    }

    /**
     * Query data from the database, without mapping results to Data Transfer Objects (DTOs).
     * <p>
     * Creates a SQL SELECT statement with the specified columns; the source table is specified
     * via a chained {@code from()} call.
     * <p>
     * This method constructs a {@link SqlFromClause} for further query composition
     * by specifying the columns to be included in the SELECT clause.
     *
     * @param columns An array of column names to be included in the SELECT statement.
     *                Each column name must be a valid, non-null string.
     * @return A {@link SqlFromClause} instance allowing further refinement of
     * the SQL query, such as specifying the table or additional clauses.
     */
    public SqlFromClause select(final String... columns) {
        return new SqlSelector(databaseProvider, tableRegistry).select(columns);
    }

    /**
     * Query data from the database, without mapping results to Data Transfer Objects (DTOs).
     * <p>
     * Creates a SQL SELECT statement with the specified columns the source table is specified
     * via a chained {@code from()} call.
     * <p>
     * This method constructs a {@link SqlFromClause} to enable further query composition.
     *
     * @param columns An array of {@link Aliased} objects representing the columns
     *                to be part of the SELECT statement. Each column must have
     *                a valid name and may optionally include an alias.
     * @return A {@link SqlFromClause} instance that allows further refinement
     * of the query, such as specifying the table or additional clauses.
     */
    public SqlFromClause select(final Aliased... columns) {
        return new SqlSelector(databaseProvider, tableRegistry).select(columns);
    }

    /**
     * Query data from the database, without mapping results to Data Transfer Object (DTOs).
     * <p>
     * Creates a SQL SELECT statement with all columns. The source table is specified
     * via a chained {@code from()} call.
     * This method constructs a {@link SqlFromClause} to enable further query composition.
     *
     * @return A {@link SqlFromClause} instance that allows further refinement
     * of the query, such as specifying the table or additional clauses.
     */
    public SqlFromClause select() {
        return new SqlSelector(databaseProvider, tableRegistry).select(ALL_COLUMNS);
    }

    /**
     * Convert a given result row into a Data Transfer Object (DTO) of the specified type.
     *
     * @param <DTO>    The type of the Data Transfer Object to be created.
     * @param row      The data row to map, containing column-value pairs. Must not be null.
     * @param dtoClass The class type of the DTO to which the row is to be mapped. Must not be null.
     * @return An instance of the specified DTO type, populated with values from the given row.
     * @throws IllegalArgumentException if the row or dtoClass is null, or if mapping fails due to type mismatches or invalid configurations.
     */
    public <DTO> DTO toDto(final Row row, final Class<DTO> dtoClass) {
        return new DefaultDtoMapper(tableRegistry, databaseProvider.getTypeConverter(), new DtoAliasRegistry())
                .toDto(row, dtoClass);
    }

    private OrmTable mapToTable(final Class<?> dtoClass, final TableSpec tableSpec) throws SQLException {
        // Up-front validation
        if (dtoClass == null) {
            throw new IllegalArgumentException("DTO class cannot be null");
        } else if (ClassUtils.isBasicType(dtoClass)) {
            throw new IllegalArgumentException("Not a DTO: " + dtoClass.getName());
        } else if (CollectionUtils.isEmpty(tableSpec.fieldColumnSpecMap())) {
            throw new IllegalArgumentException("No field-column map provided");
        }

        // Read the table metadata
        final TableMetaData tableMetaData = databaseProvider.getTableMetaData(tableSpec);

        final Map<FieldAccessor, ColumnMetaData> columnMap = mapFields(dtoClass, tableMetaData, tableSpec.fieldColumnSpecMap());
        return new OrmTable(tableMetaData, columnMap, changeTracker);
    }

    private Map<FieldAccessor, ColumnMetaData> mapFields(final Class<?> dtoClass, final TableMetaData tableMetaData, final Map<FieldSpec, ColumnSpec> fieldColumnSpecMap) {
        final Set<String> unmappedColumns = tableMetaData.columns().stream()
                .map(ColumnMetaData::name)
                .collect(Collectors.toSet());
        final Map<FieldAccessor, ColumnMetaData> mappedFields = new HashMap<>();

        // Validate and formalise field mapping
        fieldColumnSpecMap.forEach((fieldSpec, columnSpec) -> {
            if (!tableMetaData.hasColumn(columnSpec.name())) {
                throw new IllegalArgumentException(String.format("Column '%s', mapped by field spec '%s' of DTO '%s', does not exist in table: '%s'", columnSpec, fieldSpec, dtoClass, tableMetaData.name()));
            }

            if (!unmappedColumns.contains(columnSpec.name())) {
                // Column is already mapped
                final String conflictingFieldName = mappedFields.entrySet().stream()
                        .filter(fieldColumnEntry -> fieldColumnEntry.getValue().name().equals(columnSpec.name()))
                        .map(Map.Entry::getKey)
                        .map(FieldAccessor::name)
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("Conflicting field for column '%s' not found; current field spec: '%s'".formatted(columnSpec, fieldSpec)));
                throw new IllegalArgumentException(String.format("Column '%s' is already mapped by field '%s'", columnSpec, conflictingFieldName));
            }

            // Add field-column mapping
            final FieldAccessor fieldAccessor = DtoIntrospector.fieldAccessor(dtoClass, fieldSpec);
            final ColumnMetaData column = ObjectUtils.requireNonNull(tableMetaData.column(columnSpec.name()), "Column metadata not found: " + columnSpec.name());

            if (!StringUtils.isBlank(columnSpec.sequence())) {
                column.setSequence(columnSpec.sequence());
            }

            if (!ClassUtils.isBasicType(fieldAccessor.type())) {
                if (!tableRegistry.containsTable(fieldAccessor.type())) {
                    // Cascading child DTO, but no table mapping exists
                    throw new IllegalArgumentException(String.format("Sub-DTO '%s' in field '%s' of DTO '%s' is not registered", fieldAccessor.type().getName(), fieldSpec.name(), dtoClass.getName()));
                }

                if (columnSpec.joinColumn() == null) {
                    throw new IllegalArgumentException(String.format("No \"join on\" field specified for sub-DTO '%s' in field '%s' of DTO '%s'", fieldAccessor.type().getName(), fieldSpec.name(), dtoClass.getName()));
                }

                column.setJoinColumn(columnSpec.joinColumn());
            }

            mappedFields.put(fieldAccessor, column);
            unmappedColumns.remove(columnSpec.name());
        });

        // Check for unmapped columns
        if (!unmappedColumns.isEmpty()) {
            // Check if any non-nullable columns are missing
            final List<String> missingColumns = unmappedColumns.stream()
                    .filter(columnName -> !tableMetaData.column(columnName).isNullable())
                    .toList();

            if (!missingColumns.isEmpty()) {
                throw new IllegalArgumentException(String.format("Unmapped non-nullable columns for table '%s': %s; DTO class: '%s'", tableMetaData.name(), missingColumns, dtoClass.getName()));
            }
        }

        return mappedFields;
    }
}
