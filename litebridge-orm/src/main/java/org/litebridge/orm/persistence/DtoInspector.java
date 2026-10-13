package org.litebridge.orm.persistence;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.ClassUtils;
import org.litebridge.tracking.FieldAccessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Provides metadata about the state of an entity/mapped DTO.
 */
public final class DtoInspector {

    private final TableRegistry tableRegistry;

    /**
     * Creates a new {@code DtoInspector} instance.
     *
     * @param tableRegistry The Litebridge ORM table registry to use.
     */
    public DtoInspector(final TableRegistry tableRegistry) {
        this.tableRegistry = tableRegistry;
    }

    /**
     * Checks whether the primary key of the given entity/mapped DTO is set.
     *
     * @param dto The entity/mapped DTO to check.
     * @return {@code true} if the primary key is set, {@code false} otherwise.
     */
    public boolean isPrimaryKeySet(final Object dto) {
        final OrmTable ormTable = tableRegistry.getOrmTableOrThrow(dto.getClass());
        return isDtoPkSet(dto, ormTable);
    }

    public Class<?> getPrimaryKeyType(final Object dto) {
        return getPrimaryKeyType(dto.getClass());
    }

    public Class<?> getPrimaryKeyType(final Class<?> dtoClass) {
        final OrmTable ormTable = tableRegistry.getOrmTableOrThrow(dtoClass);
        return ormTable.dtoPkClass();
    }

    public <PkType> @Nullable PkType getPrimaryKey(final Object dto) {
        final OrmTable ormTable = tableRegistry.getOrmTableOrThrow(dto.getClass());
        final List<FieldAccessor> primaryKeyFields = ormTable.primaryKeyFields();

        if (primaryKeyFields.size() == 1) {
            // Basic primary key
            final FieldAccessor fieldAccessor = primaryKeyFields.getFirst();
            return (PkType) fieldAccessor.get(dto);
        } else {
            // Composite primary key
            final Class<PkType> pkType = (Class<PkType>) ormTable.dtoPkClass();

            if (List.class.isAssignableFrom(pkType)) {
                final List<@Nullable Object> pkValues = new ArrayList<>(primaryKeyFields.size());

                for (final FieldAccessor fieldAccessor : primaryKeyFields) {
                    pkValues.add(fieldAccessor.get(dto));
                }

                return (PkType) pkValues;
            } else {
                // Mapped to a specific key type
                throw new UnsupportedOperationException("Not implemented yet");
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <PkType> @Nullable PkType getPrimaryKey(final Object dto, Class<PkType> pkType) {
        final OrmTable ormTable = tableRegistry.getOrmTableOrThrow(dto.getClass());
        final List<FieldAccessor> primaryKeyFields = ormTable.primaryKeyFields();

        if (primaryKeyFields.size() == 1) {
            // Basic primary key
            final FieldAccessor fieldAccessor = primaryKeyFields.getFirst();

            if (!pkType.isAssignableFrom(fieldAccessor.type())) {
                throw new IllegalArgumentException("Primary key type mismatch; expected " + pkType.getName() + ", got " + fieldAccessor.type().getName());
            }

            return (PkType) fieldAccessor.get(dto);
        } else {
            // Composite primary key
            if (List.class.isAssignableFrom(pkType)) {
                final List<@Nullable Object> pkValues = new ArrayList<>(primaryKeyFields.size());

                for (final FieldAccessor fieldAccessor : primaryKeyFields) {
                    pkValues.add(fieldAccessor.get(dto));
                }

                return (PkType) pkValues;
            } else {
                // Mapped to a specific key type
                throw new UnsupportedOperationException("Not implemented yet");
            }
        }
    }

    /**
     * Checks whether the primary key of the given entity/mapped DTO is set.
     *
     * @param dto      The entity/mapped DTO to check.
     * @param ormTable The Litebridge ORM table metadata for the given entity/mapped DTO.
     * @return {@code true} if the primary key is set, {@code false} otherwise.
     */
    static boolean isDtoPkSet(final Object dto, final OrmTable ormTable) {
        return ormTable.metaData().primaryKey().stream().allMatch(pkColumn -> {
            final FieldAccessor embeddedDtoPkAccessor = ormTable.getFieldForColumnName(pkColumn.name());
            final Object embeddedDtoPkValue = embeddedDtoPkAccessor.get(dto);
            return !Objects.equals(embeddedDtoPkValue, ClassUtils.getDefaultValue(embeddedDtoPkAccessor.type()));
        });
    }
}
