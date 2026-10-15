package org.litebridge.spring.repository;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.ClassUtils;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.update.UpdateResult;
import org.litebridge.orm.LitebridgeCore;
import org.litebridge.orm.LitebridgeInspector;
import org.litebridge.orm.nativesql.ParsedSql;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.Parameter;
import org.springframework.data.repository.query.Parameters;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.RepositoryQuery;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LitebridgeStringQuery implements RepositoryQuery {

    private final QueryMethod queryMethod;
    private final boolean isModifying;
    private final String sql;
    private final LitebridgeCore litebridge;
    private final TypeConverter typeConverter;

    public LitebridgeStringQuery(final Method method,
                                 final RepositoryMetadata metadata,
                                 final ProjectionFactory factory,
                                 final String sql,
                                 final boolean isModifying,
                                 final LitebridgePartTreeQuery.PartTreeSpringSupport partTreeSpringSupport,
                                 final LitebridgeCore litebridge) {
        this.queryMethod = partTreeSpringSupport.createQueryMethod(method, metadata, factory);
        this.sql = sql;
        this.isModifying = isModifying;
        this.litebridge = litebridge;
        this.typeConverter = LitebridgeInspector.getDatabaseProvider(litebridge).typeConverter();
    }

    @Override
    public @Nullable Object execute(final @Nullable Object[] parameters) {
        final ParsedSql parsedSql = litebridge.nativeSql().parse(sql);
        final List<Row> rows;

        if (parsedSql.usesNamedBindParameters()) {
            final Map<String, @Nullable Object> namedParametersMap = createNamedParametersMap(parameters);

            if (isModifying) {
                final UpdateResult updateResult = litebridge.nativeSql().execute(sql, namedParametersMap);
                return updateResult.rowsAffected();
            }

            rows = litebridge.nativeSql().query(sql, namedParametersMap);
        } else {
            if (isModifying) {
                return litebridge.nativeSql().execute(sql, parameters);
            }

            rows = litebridge.nativeSql().query(sql, parameters);
        }

        final Class<?> returnType = queryMethod.getReturnedObjectType();

        if (rows.isEmpty()) {
            if (queryMethod.isCollectionQuery()) {
                return Collections.emptyList();
            }

            return ClassUtils.getDefaultValue(returnType);
        }

        if (!queryMethod.isQueryForEntity()) {
            // Return Litebridge's native Row if requested
            if (returnType == Row.class) {
                if (queryMethod.isCollectionQuery()) {
                    return rows;
                }

                return rows.getFirst();
            }

            // Convert the rows to the requested return type
            if (rows.getFirst().size() > 1) {
                throw new IllegalStateException("Expected a single column, but found " + rows.getFirst().size());
            }

            if (queryMethod.isCollectionQuery()) {
                final List<@Nullable Object> convertedResults = new ArrayList<>(rows.size());

                for (Row row : rows) {
                    final Object convertedValue = convertFirstValue(row, returnType);
                    convertedResults.add(convertedValue);
                }

                return convertedResults;
            }

            return convertFirstValue(rows.getFirst(), returnType);
        }

        if (queryMethod.isCollectionQuery()) {
            return litebridge.toDtos(rows, returnType);
        }

        return litebridge.toDto(rows.getFirst(), returnType);
    }

    @Override
    public QueryMethod getQueryMethod() {
        return this.queryMethod;
    }

    private Map<String, @Nullable Object> createNamedParametersMap(final @Nullable Object[] parameters) {
        final Map<String, @Nullable Object> namedParametersMap = new HashMap<>();
        final Parameters<?, ?> methodParameters = queryMethod.getParameters();

        for (int i = 0; i < methodParameters.getNumberOfParameters(); i++) {
            final Parameter param = methodParameters.getParameter(i);

            if (param.isSpecialParameter()) {
                // Skip special parameters like Pageable or Sort if they are passed
                continue;
            }

            String paramName = param.getName()
                    .orElseThrow(() -> new IllegalStateException("Parameter at index " + param.getIndex() + " has no name resolution available. Ensure @Param or -parameters compiler flag is used."));

            namedParametersMap.put(paramName, parameters[i]);
        }

        return namedParametersMap;
    }

    private @Nullable Object convertFirstValue(final Row row, final Class<?> returnType) {
        final Object rawValue = row.value(0);

        if (rawValue == null) {
            return ClassUtils.getDefaultValue(returnType);
        }

        return rawValue;
    }
}