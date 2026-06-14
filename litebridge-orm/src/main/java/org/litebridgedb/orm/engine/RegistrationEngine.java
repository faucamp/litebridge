package org.litebridgedb.orm.engine;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.commons.CollectionUtils;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.orm.api.register.DtoTableSpecBuilder;
import org.litebridgedb.orm.api.register.RegistrationContext;
import org.litebridgedb.orm.api.register.RegistrationContextTerminal;
import org.litebridgedb.orm.api.register.TypeSafeDtoTableMapping;
import org.litebridgedb.orm.api.spec.DtoTableSpec;
import org.litebridgedb.orm.persistence.OrmTable;
import org.litebridgedb.orm.persistence.TableMapper;
import org.litebridgedb.orm.persistence.TableRegistry;
import org.litebridgedb.orm.persistence.TransactionalDatabaseProvider;
import org.litebridgedb.orm.persistence.register.AnnotationMapper;
import org.litebridgedb.tracking.ChangeTracker;
import org.litebridgedb.tracking.FieldAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class RegistrationEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegistrationEngine.class);

    private final TransactionalDatabaseProvider databaseProvider;
    private final TableRegistry tableRegistry;
    private final TableMapper tableMapper;
    private final ChangeTracker changeTracker;
    private final MethodHandles.Lookup lookup;
    private @Nullable List<FieldAccessor> pendingManyToOneDependencies;

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
     * Registers DTO table specification(s) using the provided type-safe DTO table mapping(s).
     *
     * @param typeSafeDtoTableMappings one or more type-safe DTO table mappings to create and register DTO table specifications for
     */
    public void register(final TypeSafeDtoTableMapping... typeSafeDtoTableMappings) {
        final DtoTableSpec[] dtoTableSpecs = Arrays.stream(typeSafeDtoTableMappings)
                .map(typeSafeDtoTableMapping -> typeSafeDtoTableMapping.createDtoTableSpec(databaseProvider))
                .toArray(DtoTableSpec[]::new);

        register(dtoTableSpecs);
    }

    /**
     * Registers annotated entity class(es).
     * <p>
     * The annotated entity must be annotated with {@link Table} and contain at least one field annotated with
     * {@link org.litebridgedb.orm.annotation.Column}, {@link org.litebridgedb.orm.annotation.OneToMany} or {@link org.litebridgedb.orm.annotation.ManyToMany}.
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
                final OrmTable targetOrmTable = tableRegistry.getTable(fieldAccessor.genericType());

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
