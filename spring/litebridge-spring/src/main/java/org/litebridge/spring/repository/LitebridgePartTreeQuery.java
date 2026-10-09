package org.litebridge.spring.repository;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.LitebridgeCore;
import org.litebridge.orm.api.condition.DtoWhereCriteriaBuilder;
import org.litebridge.orm.api.select.LimitClauseTerminal;
import org.litebridge.orm.api.select.OrderByClauseTerminal;
import org.litebridge.orm.api.select.dto.DtoFromClauseTerminal;
import org.litebridge.orm.expression.Fn;
import org.springframework.data.domain.Sort;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.ParametersParameterAccessor;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.data.repository.query.parser.PartTree;

import java.lang.reflect.Method;
import java.util.Objects;

public class LitebridgePartTreeQuery implements RepositoryQuery {

    private final QueryMethod queryMethod;
    private final PartTree tree;
    private final Class<?> entityClass;
    private final LitebridgeCore litebridge;

    public LitebridgePartTreeQuery(final Method method,
                                   final RepositoryMetadata metadata,
                                   final ProjectionFactory projectionFactory,
                                   final LitebridgeCore litebridge) {
        // Using deprecated method for Spring Boot 3.2 compatibility
        this.queryMethod = new QueryMethod(method, metadata, projectionFactory);
        this.entityClass = metadata.getDomainType();
        this.litebridge = litebridge;
        this.tree = new PartTree(method.getName(), entityClass);
    }

    @Override
    public @Nullable Object execute(final @Nullable Object[] parameters) {
        ParametersParameterAccessor accessor = new ParametersParameterAccessor(queryMethod.getParameters(), parameters);

        // Build the Litebridge criteria from the PartTree
        final DtoFromClauseTerminal<?> select;

        if (tree.isCountProjection()) {
            select = litebridge.select(Fn.count()).from(entityClass);
        } else {
            select = litebridge.select(entityClass);
        }

        final DtoWhereCriteriaBuilder<?> criteriaBuilder = new DtoWhereCriteriaBuilder<>(select);
        int paramIndex = 0;

        for (PartTree.OrPart orPart : tree) {
            // Traverse AND conditions inside each OR group
            for (Part part : orPart) {
                String propertyPath = part.getProperty().toDotPath();
                Part.Type type = part.getType();

                switch (type) {
                    case SIMPLE_PROPERTY -> {
                        final Object value = accessor.getBindableValue(paramIndex++);
                        criteriaBuilder.add(q -> q.where(propertyPath).eq(value));
                    }
                    case NEGATING_SIMPLE_PROPERTY -> {
                        final Object value = accessor.getBindableValue(paramIndex++);
                        criteriaBuilder.add(q -> q.where(propertyPath).neq(value));
                    }
                    case GREATER_THAN -> {
                        final Object value = Objects.requireNonNull(accessor.getBindableValue(paramIndex++), "Value cannot be null for GREATER_THAN");
                        criteriaBuilder.add(q -> q.where(propertyPath).gt(value));
                    }
                    case GREATER_THAN_EQUAL -> {
                        final Object value = Objects.requireNonNull(accessor.getBindableValue(paramIndex++), "Value cannot be null for GREATER_THAN_EQUAL");
                        criteriaBuilder.add(q -> q.where(propertyPath).gte(value));
                    }
                    case LESS_THAN -> {
                        final Object value = Objects.requireNonNull(accessor.getBindableValue(paramIndex++), "Value cannot be null for LESS_THAN");
                        criteriaBuilder.add(q -> q.where(propertyPath).lt(value));
                    }
                    case LESS_THAN_EQUAL -> {
                        final Object value = Objects.requireNonNull(accessor.getBindableValue(paramIndex++), "Value cannot be null for LESS_THAN_EQUAL");
                        criteriaBuilder.add(q -> q.where(propertyPath).lte(value));
                    }
                    case LIKE, CONTAINING -> {
                        final String value = Objects.requireNonNull(accessor.getBindableValue(paramIndex++), "Value cannot be null for LIKE / CONTAINING").toString();
                        final String likeStr = '%' + value + '%';
                        criteriaBuilder.add(q -> q.where(propertyPath).like(likeStr));
                    }
                    case NOT_LIKE, NOT_CONTAINING -> {
                        final String value = Objects.requireNonNull(accessor.getBindableValue(paramIndex++), "Value cannot be null for NOT_LIKE / NOT_CONTAINING").toString();
                        final String likeStr = '%' + value + '%';
                        criteriaBuilder.add(q -> q.where(propertyPath).notLike(likeStr));
                    }
                    case STARTING_WITH -> {
                        final String value = Objects.requireNonNull(accessor.getBindableValue(paramIndex++), "Value cannot be null for NOT_LIKE").toString();
                        final String likeStr = value + '%';
                        criteriaBuilder.add(q -> q.where(propertyPath).like(likeStr));
                    }
                    case ENDING_WITH -> {
                        final String value = Objects.requireNonNull(accessor.getBindableValue(paramIndex++), "Value cannot be null for NOT_LIKE").toString();
                        final String likeStr = '%' + value;
                        criteriaBuilder.add(q -> q.where(propertyPath).like(likeStr));
                    }
                    case IS_NULL -> criteriaBuilder.add(q -> q.where(propertyPath).isNull());
                    case IS_NOT_NULL -> criteriaBuilder.add(q -> q.where(propertyPath).isNotNull());
                    case IN -> {
                        final Object value = Objects.requireNonNull(accessor.getBindableValue(paramIndex++), "Value cannot be null for IN");

                        if (value instanceof Iterable<?> iterable) {
                            criteriaBuilder.add(q -> q.where(propertyPath).in(iterable));
                        } else {
                            criteriaBuilder.add(q -> q.where(propertyPath).in(value));
                        }
                    }
                    case NOT_IN -> {
                        final Object value = Objects.requireNonNull(accessor.getBindableValue(paramIndex++), "Value cannot be null for IN");

                        if (value instanceof Iterable<?> iterable) {
                            criteriaBuilder.add(q -> q.where(propertyPath).notIn(iterable));
                        } else {
                            criteriaBuilder.add(q -> q.where(propertyPath).notIn(value));
                        }
                    }
                    case TRUE -> criteriaBuilder.add(q -> q.where(propertyPath).eq(true));
                    case FALSE -> criteriaBuilder.add(q -> q.where(propertyPath).eq(false));
                    default -> throw new UnsupportedOperationException("Unsupported query part type: " + type);
                }

                criteriaBuilder.setLogicOperator(LogicOperator.AND);
            }

            criteriaBuilder.setLogicOperator(LogicOperator.OR);
        }

        OrderByClauseTerminal<?> orderByClauseTerminal = criteriaBuilder.build();

        // Apply Sorting
        if (tree.getSort().isSorted()) {
            // Map Spring Data Sort specs to Litebridge ORDER BY
            for (Sort.Order order : tree.getSort()) {
                if (order.isAscending()) {
                    orderByClauseTerminal = select.orderBy(order.getProperty()).asc();
                } else {
                    orderByClauseTerminal = select.orderBy(order.getProperty()).desc();
                }
            }
        }

        // Apply Limits (e.g., findFirst10By...)
        LimitClauseTerminal<?> limitClauseTerminal = orderByClauseTerminal;

        if (tree.isLimiting()) {
            limitClauseTerminal = orderByClauseTerminal.limit(Objects.requireNonNull(tree.getMaxResults()));
        }

        // Execute according to return type
        if (queryMethod.isCollectionQuery()) {
            return limitClauseTerminal.list();
        } else if (queryMethod.isQueryForEntity()) {
            return limitClauseTerminal.oneOrNull();
        } else if (tree.isCountProjection()) {
            return limitClauseTerminal.oneOrThrow();
        } else if (tree.isExistsProjection()) {
            return limitClauseTerminal.oneOrThrow();
        }

        return limitClauseTerminal.list();
    }

    @Override
    public QueryMethod getQueryMethod() {
        return this.queryMethod;
    }
}