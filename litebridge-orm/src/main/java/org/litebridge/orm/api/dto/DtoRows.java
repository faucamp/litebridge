package org.litebridge.orm.api.dto;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.query.Result;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Deprecated(forRemoval = true)
public final class DtoRows implements Result {

    private final Map<Class<?>, List<Row>> rowsPerDto = new HashMap<>();

    public void add(final Class<?> dtoClass, final List<Row> rows) {
        if (rowsPerDto.containsKey(dtoClass)) {
            final List<Row> existingRows = rowsPerDto.get(dtoClass);
            rows.forEach(row -> {
                if (!existingRows.contains(row)) {
                    existingRows.add(row);
                }
            });
        } else {
            rowsPerDto.put(dtoClass, rows);
        }
    }

    public @Nullable List<Row> rows(final Class<?> dtoClass) {
        return rowsPerDto.get(dtoClass);
    }

    public Stream<DtoClassRows> streamOmit(final Class<?> omitClass) {
        return rowsPerDto.entrySet().stream()
                .filter(entry -> entry.getKey() != omitClass)
                .map(entry -> new DtoClassRows(entry.getKey(), entry.getValue()));
    }

    public record DtoClassRows(Class<?> dtoClass, List<Row> rows) {
    }
}
