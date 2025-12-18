package org.litebridge.orm.api.select.sql;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.litebridge.commons.ObjectUtils;
import org.litebridge.db.api.DatabaseProvider;
import org.litebridge.db.api.TableMetaData;
import org.litebridge.orm.api.select.AbstractSelector;
import org.litebridge.orm.api.select.Condition;
import org.litebridge.orm.api.select.SelectorTerminal;
import org.litebridge.orm.exception.NonUniqueResultException;
import org.litebridge.orm.persistence.DtoMapper;
import org.litebridge.orm.persistence.Table;
import org.litebridge.orm.persistence.TableRegistry;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

@NullMarked
public final class SqlSelector extends AbstractSelector<Map<String, Object>, SqlConditionTerminal> implements SqlConditionTerminal {

    private final TableRegistry tableRegistry;

    public SqlSelector(final List<String> columns, final TableMetaData tableMetaData, final TableRegistry tableRegistry, final DatabaseProvider databaseProvider) {
        super(columns, tableMetaData, databaseProvider);
        this.tableRegistry = tableRegistry;
    }

    @Override
    public Condition<Map<String, Object>, SqlConditionTerminal> and(final String column) {
        return where(column);
    }

    @Override
    public <T> SelectorTerminal<T> mapToDto(Class<T> dtoClass) {
        return new SqlSelectorMappedTerminal<>(dtoClass);
    }

    @Override
    public @Nullable Map<String, Object> oneOrNull() {
        return super.getOneRecord(false);
    }

    @Override
    public @Nullable Map<String, Object> firstOrNull() {
        return super.getOneRecord(true);
    }

    @Override
    public List<Map<String, Object>> list() {
        return super.getAllRecords();
    }

    @Override
    public Stream<Map<String, Object>> stream() {
        return super.streamRecords();
    }

    @Override
    protected Condition<Map<String, Object>, SqlConditionTerminal> createCondition(final String column) {
        return new SqlCondition(column, this);
    }

    public class SqlSelectorMappedTerminal<T> implements SelectorTerminal<T> {

        private final Class<T> dtoClass;

        public SqlSelectorMappedTerminal(final Class<T> dtoClass) {
            this.dtoClass = dtoClass;
        }

        @Override
        public Optional<T> one() {
            return Optional.ofNullable(oneOrNull());
        }

        @Override
        public @Nullable T oneOrNull() throws NonUniqueResultException {
            return mapToDto(SqlSelector.this.oneOrNull());
        }

        @Override
        public T oneOrThrow() throws NoSuchElementException {
            return oneOrThrow(() -> new NoSuchElementException("No record found for query"));
        }

        @Override
        public <X extends Throwable> T oneOrThrow(final Supplier<? extends X> exceptionSupplier) throws X {
            return ObjectUtils.requireNonNull(oneOrNull(), exceptionSupplier);
        }

        @Override
        public Optional<T> first() {
            return Optional.ofNullable(firstOrNull());
        }

        @Override
        public @Nullable T firstOrNull() {
            return mapToDto(SqlSelector.this.firstOrNull());
        }

        @Override
        public T firstOrThrow() throws NoSuchElementException {
            return firstOrThrow(() -> new NoSuchElementException("No record found for query"));
        }

        @Override
        public <X extends Throwable> T firstOrThrow(final Supplier<? extends X> exceptionSupplier) throws X {
            return ObjectUtils.requireNonNull(firstOrNull(), exceptionSupplier);
        }

        @Override
        public Stream<T> stream() {
            return SqlSelector.this.stream()
                    .map(this::mapToDto);
        }

        @Override
        public List<T> list() {
            return stream().toList();
        }

        private @Nullable T mapToDto(@Nullable final Map<String, Object> record) {
            final Table table = tableRegistry.getTableOrThrow(dtoClass);

            if (record != null) {
                return DtoMapper.mapToDto(record, dtoClass, table, databaseProvider.getTypeConverter());
            } else {
                return null;
            }
        }
    }
}