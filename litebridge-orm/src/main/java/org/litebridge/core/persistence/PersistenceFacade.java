package org.litebridge.core.persistence;

import org.litebridge.commons.CollectionUtils;
import org.litebridge.commons.ClassUtils;
import org.litebridge.core.Table;
import org.litebridge.core.TableRegistry;
import org.litebridge.tracking.ChangedField;
import org.litebridge.tracking.TrackedDto;
import org.litebridge.db.api.Column;
import org.litebridge.db.api.DatabaseProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PersistenceFacade {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersistenceFacade.class);
    private final TableRegistry tableRegistry;
    private final DatabaseProvider databaseProvider;

    public PersistenceFacade(final TableRegistry tableRegistry,
                             final DatabaseProvider databaseProvider) {
        this.tableRegistry = tableRegistry;
        this.databaseProvider = databaseProvider;
    }

    public void save(final Object dto) throws SQLException {
        if (dto == null) {
            throw new IllegalArgumentException("DTO cannot be null");
        }

        final Table table = tableRegistry.getTable(dto.getClass());

        if (table == null) {
            throw new IllegalArgumentException("DTO class not registered: '%s'".formatted(dto.getClass().getName()));
        }

        final TrackedDto trackedDto = table.getTrackedDto(dto);

        if (trackedDto == null) {
            throw new IllegalArgumentException("DTO not tracked: '%s'".formatted(dto.toString()));
        }

        final Map<String, ChangedField> changedFields = trackedDto.getChangedFields(dto);

        if (CollectionUtils.isEmpty(changedFields)) {
            LOGGER.debug("No changed fields found for DTO: {}", dto);
            return;
        }

        if (LOGGER.isDebugEnabled()) {
            final StringBuilder sb = new StringBuilder("Changed fields for DTO: ").append(dto).append("\n");

            changedFields.entrySet().forEach(entry -> {
                sb.append("\t").append(entry.getKey()).append(" = ").append(entry.getValue().value()).append("\n");
            });

            LOGGER.debug(sb.toString());
        }

        final Map<String, Object> columnValues = new LinkedHashMap<>();

        for (Column column : table.getMetaData().getColumns().values()) {
            final String fieldName = table.getFieldForColumnName(column.getName()).getName();
            final ChangedField changedField = changedFields.get(fieldName);
            final Object value;
            final boolean basicType;

            if (changedField == null) {
                if (column.isAutoIncrement() || column.getSequence() != null) {
                    basicType = true;
                    value = null;
                } else {
                    continue;
                }
            } else {
                basicType = changedField.value() == null || ClassUtils.isBasicType(changedField.value().getClass());
                value = changedField.value();
            }

            if (basicType) {
                columnValues.put(column.getName(), value);
            } else if (!table.isPersistedDto(value)) {
                // Cascade save to the embedded DTO
                save(value);
                // Retrieve the PK
                final Table embeddedDtoTable = tableRegistry.getTable(value.getClass());
                // TODO: composite PK support
                final List<String> embeddedDtoPk = embeddedDtoTable.getMetaData().getPrimaryKey();
                final Field field = embeddedDtoTable.getFieldForColumnName(embeddedDtoPk.get(0));

                try {
                    columnValues.put(column.getName(), field.get(value));
                } catch (IllegalAccessException ex) {
                    throw new IllegalStateException("Failed to retrieve PK from cascaded DTO: " + value, ex);
                }
            }
        }

        if (table.isPersistedDto(dto)) {
            update(dto, table, columnValues);
        } else {
            insert(dto, table, columnValues);
        }

        table.syncPersistedDto(dto);
    }

    private void insert(final Object dto, final Table table, final Map<String, Object> columnValues) throws SQLException {
        table.getMetaData().getPrimaryKey().stream()
                .filter(pk -> !columnValues.containsKey(pk))
                .forEach(pk -> {
                    final Field pkField = Objects.requireNonNull(table.getFieldForColumnName(pk), "Missing field for PK column: " + pk);
                    final Object pkValue;

                    try {
                        pkValue = pkField.get(dto);
                    } catch (IllegalAccessException ex) {
                        throw new IllegalStateException("Failed to retrieve PK field '%s' of DTO: %s".formatted(pkField.getName(), dto), ex);
                    }

                    columnValues.put(pk, pkValue);
                });


        final List<Object> generatedKeys = databaseProvider.insert(table.getMetaData(), columnValues);

        if (!CollectionUtils.isEmpty(generatedKeys)) {
            table.getMetaData().getPrimaryKey().forEach(pk -> {
                final Object pkValue = generatedKeys.get(table.getMetaData().getPrimaryKey().indexOf(pk));
                final Field pkField = Objects.requireNonNull(table.getFieldForColumnName(pk), "Missing field for PK column: " + pk);

                try {
                    pkField.set(dto, pkValue);
                } catch (IllegalAccessException ex) {
                    throw new IllegalStateException("Failed to set PK field '%s' of DTO: %s".formatted(pkField.getName(), dto), ex);
                }
            });
        }
    }

    private void update(final Object dto, final Table table, final Map<String, Object> columnValues) throws SQLException {
        // Extract the PK
        final LinkedHashMap<String, Object> primaryKey = table.getMetaData().getPrimaryKey().stream()
                .collect(Collectors.toMap(Function.identity(),
                        pkColumn -> {
                            Object pkValue = columnValues.remove(pkColumn);

                            if (pkValue == null) {
                                final Field pkField = table.getFieldForColumnName(pkColumn);
                                try {
                                    pkValue = pkField.get(dto);
                                } catch (IllegalAccessException ex) {
                                    throw new IllegalStateException("Failed to retrieve PK field '%s' of DTO: %s".formatted(pkField.getName(), dto), ex);
                                }
                            }

                            return pkValue;
                        },
                        (oldValue, newValue) -> newValue,
                        LinkedHashMap::new));

        databaseProvider.update(table.getMetaData(), columnValues, primaryKey);
    }
}
