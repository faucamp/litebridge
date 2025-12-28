package org.litebridge.db.spi;

import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Stream;

public final class Row {
    private final LinkedHashMap<Column, Object> columns = new LinkedHashMap<>();

    public Row withColumn(final Column column, final Object value) {
        columns.put(column, value);
        return this;
    }

    public Stream<RowColumn> columnStream() {
        return columns.keySet().stream()
                .map(RowColumn::new);
    }

    public Optional<RowColumn> column(final String column) {
        return columnStream()
                .filter(rc -> Objects.equals(rc.column().name(), column))
                .findFirst();
    }

    @Override
    public String toString() {
        final StringJoiner sj = new StringJoiner(", ", "{", "}");
        columns.forEach((column, value) -> sj.add(column.name()
                + (column.alias() != null && !Objects.equals(column.alias(), column.name()) ? "/" + column.alias() + "=" : "=")
                + value));
        return sj.toString();
    }

    public final class RowColumn {
        private final Column column;

        public RowColumn(final Column column) {
            this.column = column;
        }

        public Column column() {
            return column;
        }

        public Object value() {
            return columns.get(column);
        }

        @Override
        public String toString() {
            if (column.alias() != null) {
                return column.name() + "/" + column.alias() + "=" + value();
            } else {
                return column.name() + "=" + value();
            }
        }
    }
}
