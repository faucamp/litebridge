package org.litebridge.orm.engine;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.CollectionUtils;
import org.litebridge.db.spi.Table;
import org.litebridge.orm.api.register.DtoTableSpecBuilder;
import org.litebridge.orm.api.register.RegistrationContext;
import org.litebridge.orm.api.register.RegistrationContextTerminal;
import org.litebridge.orm.api.spec.DtoTableSpec;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.orm.persistence.TableMapper;
import org.litebridge.orm.persistence.TableRegistry;
import org.litebridge.orm.persistence.TransactionalDatabaseProvider;
import org.litebridge.orm.persistence.register.AnnotationMapper;
import org.litebridge.tracking.ChangeTracker;
import org.litebridge.tracking.FieldAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * The RegistrationEngine class handles the registration of data transfer objects (DTOs) and their
 * corresponding table specifications to enable mapping between application entities and database tables.
 * <p>
 * This class supports various registration methods for defining table structures, relationships, and
 * metadata for database operations.
 */
public class RegistrationEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegistrationEngine.class);

    private final TransactionalDatabaseProvider databaseProvider;
    private final TableRegistry tableRegistry;
    private final TableMapper tableMapper;
    private final ChangeTracker changeTracker;
    private final MethodHandles.Lookup lookup;
    private @Nullable List<FieldAccessor> pendingManyToOneDependencies;

    /**
     * Creates a new RegistrationEngine.
     *
     * @param databaseProvider the database provider to use
     * @param tableRegistry the registry for DTO-to-table mappings
     * @param tableMapper the mapper for converting DTO definitions to table definitions
     * @param changeTracker the tracker for monitoring DTO changes
     * @param lookup the lookup for accessing DTO fields
     */
    public RegistrationEngine(final TransactionalDatabaseProvider databaseProvider, final TableRegistry tableRegistry, final TableMapper tableMapper, final ChangeTracker changeTracker, final MethodHandles.Lookup lookup) {
        this.databaseProvider = databaseProvider;
        this.tableRegistry = tableRegistry;
        this.tableMapper = tableMapper;
        this.changeTracker = changeTracker;
        this.lookup = lookup;
    }

    /**
     * Registers a DTO class along with its associated table specification using the provided registration context function.
     *
     * @param dtoClass The class object of the DTO (Data Transfer Object) to be registered.
     * @param rc       A function that takes a RegistrationContext instance to configure the table mapping.
     */
    public void register(final Class<?> dtoClass, final Function<RegistrationContext, RegistrationContextTerminal> rc) {
        final RegistrationContextTerminal context = rc.apply(new RegistrationContext(dtoClass, databaseProvider));
        register(new DtoTableSpecBuilder(context).build());
    }

    /**
     * Registers annotated entity class(es).
     * <p>
     * The annotated entity must be annotated with {@link Table} and contain at least one field annotated with
     * {@link org.litebridge.orm.annotation.Column}, {@link org.litebridge.orm.annotation.OneToMany} or {@link org.litebridge.orm.annotation.ManyToMany}.
     *
     * @param entityClasses the class(es) of the entity/entities to be registered.
     */
    public void register(final Class<?>... entityClasses) {
        final DtoTableSpec[] dtoTableSpecs = new DtoTableSpec[entityClasses.length];

        for (int i = 0; i < entityClasses.length; i++) {
            LOGGER.debug("Registering entity class '{}'", entityClasses[i]);
            dtoTableSpecs[i] = AnnotationMapper.createDtoTableSpec(entityClasses[i], databaseProvider, lookup);
        }

        register(dtoTableSpecs);
    }

    /**
     * Register a Data Transfer Object (DTO) class(es) with its corresponding table specification(s).
     * <p>
     * This method maps the DTO class to a database table and stores the association
     * in the table registry to enable database operations such as insert, update, or query.
     *
     * @param dtoTableSpecs One or more DTO-to-table mapping details
     */
    public void register(final DtoTableSpec... dtoTableSpecs) {
        final Set<Class<?>> allDtoClasses = new HashSet<>(dtoTableSpecs.length);

        for (final DtoTableSpec dtoTableSpec : dtoTableSpecs) {
            allDtoClasses.add(dtoTableSpec.dtoClass());
            allDtoClasses.addAll(dtoTableSpec.dtoInterfaces());
        }

        for (final DtoTableSpec dtoTableSpec : dtoTableSpecs) {
            final Class<?> dtoClass = dtoTableSpec.dtoClass();
            try {
                final MethodHandles.Lookup elevatedLookup = MethodHandles.privateLookupIn(dtoClass, lookup);
                changeTracker.classFieldAccessorCache().registerElevatedLookup(dtoClass, elevatedLookup);
            } catch (IllegalAccessException e) {
                // If we can't create a private lookup, the tracking will fall back to the provided lookup
                // which might fail if the module is not open to litebridge-tracking.
                // This is expected if the user hasn't opened their module to litebridge.orm either.
                LOGGER.warn("Failed to create elevated lookup for DTO class '{}'. Ensure the module is open to litebridge.orm.", dtoClass.getName());
            }

            LOGGER.trace("Registering DtoTableSpec for DTO class '{}'", dtoClass);
            final TableMapper.MappedTable mappedTable = tableMapper.mapToTable(lookup, dtoClass, dtoTableSpec.tableSpec(), allDtoClasses);
            final OrmTable ormTable = mappedTable.ormTable();
            tableRegistry.addTable(dtoTableSpec.dtoClass(), ormTable);

            if (!CollectionUtils.isEmpty(dtoTableSpec.dtoInterfaces())) {
                ormTable.setDtoClassInterfaces(new HashSet<>(dtoTableSpec.dtoInterfaces()));
                dtoTableSpec.dtoInterfaces().forEach(dtoInterface -> tableRegistry.addTable(dtoInterface, ormTable));
            }

            if (!ormTable.getNestedDtoClasses().isEmpty()) {
                ormTable.getNestedDtoClasses().forEach(nestedDtoClass -> tableRegistry.addTable(nestedDtoClass, ormTable));
            }

            // Process pending many-to-one dependencies for this class
            if (!CollectionUtils.isEmpty(pendingManyToOneDependencies)) {
                final Iterator<FieldAccessor> iterator = pendingManyToOneDependencies.iterator();

                while (iterator.hasNext()) {
                    final FieldAccessor fieldAccessor = iterator.next();

                    if (fieldAccessor.genericType() == dtoTableSpec.dtoClass()) {
                        ormTable.addOneToManyReverseMapping(fieldAccessor);
                        iterator.remove();
                    }
                }
            }

            // Process/pend this table's dependants)
            mappedTable.manyToOneDependencies().forEach(fieldAccessor -> {
                final OrmTable targetOrmTable = tableRegistry.getOrmTable(fieldAccessor.genericType());

                if (targetOrmTable != null) {
                    targetOrmTable.addOneToManyReverseMapping(fieldAccessor);
                } else {
                    if (pendingManyToOneDependencies == null) {
                        pendingManyToOneDependencies = new ArrayList<>();
                    }

                    pendingManyToOneDependencies.add(fieldAccessor);
                }
            });
        }
    }
}
