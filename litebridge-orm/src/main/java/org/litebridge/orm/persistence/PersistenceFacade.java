package org.litebridge.orm.persistence;

import org.litebridge.commons.ClassUtils;
import org.litebridge.commons.CollectionUtils;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.tracking.ChangedField;
import org.litebridge.tracking.ChangedFields;
import org.litebridge.tracking.FieldAccessor;
import org.litebridge.tracking.TrackedDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
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
        final Table table = tableRegistry.getTable(dto.getClass());

        if (table == null) {
            throw new IllegalArgumentException("DTO class not registered: '%s'".formatted(dto.getClass().getName()));
        }

        final TrackedDto<?> trackedDto = table.getTrackedDto(dto);

        if (trackedDto == null) {
            throw new IllegalArgumentException("DTO not tracked: '%s'".formatted(dto.toString()));
        }

        final ChangedFields changedFields = trackedDto.getChangedFields();

        if (changedFields.isEmpty()) {
            LOGGER.debug("No changed fields found for DTO: {}", dto);
            table.syncPersistedDto(dto);
            return;
        }

        if (LOGGER.isTraceEnabled()) {
            final StringJoiner sj = new StringJoiner(",", "[", "]");
            changedFields.forEach(changedField -> sj.add(changedField.name() + " = " + changedField.value()));
            LOGGER.trace("Changed fields for DTO: {}: {}", dto, sj);
        }

        final Map<String, Object> columnValues = new LinkedHashMap<>();

        for (ColumnMetaData column : table.getMetaData().columns()) {
            final String fieldName = table.getFieldForColumnName(column.name()).name();
            final ChangedField changedField = changedFields.getOrNull(fieldName);
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
                basicType = ClassUtils.isBasicType(changedField.value().getClass());
                value = changedField.value();
            }

            if (basicType) {
                columnValues.put(column.name(), value);
            } else if (!table.isPersistedDto(value)) {
                // Cascade save to the embedded DTO
                save(value);
                // Retrieve the PK
                final Table embeddedDtoTable = tableRegistry.getTable(value.getClass());
                // TODO: composite PK support
                final List<String> embeddedDtoPk = embeddedDtoTable.getMetaData().primaryKey();
                final FieldAccessor field = embeddedDtoTable.getFieldForColumnName(embeddedDtoPk.get(0));
                columnValues.put(column.name(), field.get(value));
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
        table.getMetaData().primaryKey().stream()
                .filter(pk -> !columnValues.containsKey(pk))
                .forEach(pk -> {
                    final FieldAccessor pkField = table.getFieldForColumnName(pk);
                    columnValues.put(pk, pkField.get(dto));
                });


        final List<Object> generatedKeys = databaseProvider.insert(table.getMetaData(), columnValues);

        if (!CollectionUtils.isEmpty(generatedKeys)) {
            table.getMetaData().primaryKey().forEach(pk -> {
                final Object pkValue = generatedKeys.get(table.getMetaData().primaryKey().indexOf(pk));
                final FieldAccessor pkField = table.getFieldForColumnName(pk);
                pkField.set(dto, pkValue);
            });
        }
    }

    private void update(final Object dto, final Table table, final Map<String, Object> columnValues) throws SQLException {
        // Extract the PK
        final LinkedHashMap<String, Object> primaryKey = table.getMetaData().primaryKey().stream()
                .collect(Collectors.toMap(Function.identity(),
                        pkColumn -> {
                            Object pkValue = columnValues.remove(pkColumn);

                            if (pkValue == null) {
                                final FieldAccessor pkField = table.getFieldForColumnName(pkColumn);
                                pkValue = pkField.get(dto);
                            }

                            return pkValue;
                        },
                        (oldValue, newValue) -> newValue,
                        LinkedHashMap::new));

        databaseProvider.update(table.getMetaData(), columnValues, primaryKey);
    }
}
