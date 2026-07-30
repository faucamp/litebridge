package org.litebridge.orm.engine;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.orm.api.dto.DtoDataSpec;
import org.litebridge.orm.api.dto.DtoSelectSpec;
import org.litebridge.orm.api.select.ast.BeginGroupNode;
import org.litebridge.orm.api.select.ast.ConditionGroupNode;
import org.litebridge.orm.api.select.ast.ConditionNode;
import org.litebridge.orm.api.select.ast.EndGroupNode;
import org.litebridge.orm.api.select.ast.FromNode;
import org.litebridge.orm.api.select.ast.GroupByNode;
import org.litebridge.orm.api.select.ast.HavingNode;
import org.litebridge.orm.api.select.ast.JoinNode;
import org.litebridge.orm.api.select.ast.LimitNode;
import org.litebridge.orm.api.select.ast.OrderByNode;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.ast.SelectNode;
import org.litebridge.orm.api.select.ast.WhereNode;
import org.litebridge.orm.api.select.model.ConditionGroupSpec;
import org.litebridge.orm.api.select.model.ConditionSpec;
import org.litebridge.orm.api.select.model.JoinSpec;
import org.litebridge.orm.api.select.model.SelectSpec;
import org.litebridge.orm.api.sql.SqlJoinSpec;
import org.litebridge.orm.api.sql.SqlSelectSpec;
import org.litebridge.orm.expression.ColumnExpressionSpec;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.orm.persistence.TableRegistry;
import org.litebridge.orm.persistence.alias.AliasGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Compiles a {@link QueryNode} chain into a {@link SelectSpec}.
 */
public final class QueryCompiler {

    private final TableRegistry tableRegistry;
    private final AliasGenerator aliasGenerator;
    private final Map<Class<?>, List<Table>> aliasHistory = new HashMap<>();
    private final Map<Table, OrmTable> tableToOrmTableMap = new HashMap<>();

    public QueryCompiler(final TableRegistry tableRegistry, final AliasGenerator aliasGenerator) {
        this.tableRegistry = tableRegistry;
        this.aliasGenerator = aliasGenerator;
    }

    /**
     * Compiles the given {@link QueryNode} chain into the provided {@link SelectSpec}.
     *
     * @param node       the end of the query node chain
     * @param selectSpec the select specification to populate
     */
    public void compile(final QueryNode node, final SelectSpec selectSpec) {
        if (selectSpec instanceof DtoDataSpec dds) {
            tableToOrmTableMap.put(selectSpec.getTable(), dds.dtoTable());
            aliasHistory.computeIfAbsent(dds.dtoTable().dtoClass(), k -> new ArrayList<>()).add(selectSpec.getTable());
        }

        final List<QueryNode> nodes = flatten(node);

        for (final QueryNode n : nodes) {
            applyNode(n, selectSpec);
        }
    }

    private void applyNode(final QueryNode node, final SelectSpec selectSpec) {
        applyNode(node, null, selectSpec);
    }

    private void applyNode(final QueryNode node, final @Nullable QueryNode parentNode, final SelectSpec selectSpec) {
        switch (node) {
            case SelectNode selectNode -> {
                if (selectNode.expressions().length > 0) {
                    selectSpec.setExpressions(List.of(selectNode.expressions()));
                }
            }
            case FromNode fromNode -> {
                if (selectSpec instanceof SqlSelectSpec sqlSelectSpec && fromNode.tableName() != null) {
                    final Table spiTable = tableRegistry.getOrCreateSpiTable(fromNode.tableName());
                    sqlSelectSpec.setTable(spiTable);
                }
            }
            case JoinNode joinNode -> {
                if (selectSpec instanceof DtoSelectSpec dtoSelectSpec && joinNode.dtoClass() != null) {
                    final OrmTable joinOrmTable;
                    if (joinNode.sourceDtoClass() != null) {
                        joinOrmTable = tableRegistry.getTableInContext(joinNode.dtoClass(), joinNode.sourceDtoClass())
                                .orElseGet(() -> tableRegistry.getTableOrThrow(joinNode.dtoClass()));
                    } else {
                        joinOrmTable = tableRegistry.getTableOrThrow(joinNode.dtoClass());
                    }

                    final Table joinTable = aliasGenerator.aliasTable(joinOrmTable);
                    tableToOrmTableMap.put(joinTable, joinOrmTable);

                    if (joinNode.tableName() != null) {
                        // Intermediate join table (many-to-many)
                        final SqlJoinSpec joinSpec = new SqlJoinSpec(joinTable, dtoSelectSpec.selectExpressionMapper());
                        dtoSelectSpec.addJoin(joinSpec);
                    } else {
                        // Standard DTO join
                        final org.litebridge.orm.api.dto.DtoJoinSpec joinSpec = new org.litebridge.orm.api.dto.DtoJoinSpec(
                                joinNode.dtoClass(),
                                joinOrmTable,
                                joinTable,
                                dtoSelectSpec.getSelectExpressionMapper());
                        joinSpec.setSourceDtoClass(joinNode.sourceDtoClass());

                        // Extend selects with joined table columns
                        final List<org.litebridge.orm.expression.select.SelectFieldSpec> joinFieldColumns = joinOrmTable.getMetaData().columns().stream()
                                .map(joinColumn -> {
                                    final org.litebridge.tracking.FieldAccessor joinColumnField = joinOrmTable.fieldForColumnNameOrNull(joinColumn.name());

                                    if (joinColumnField == null) {
                                        return null;
                                    }

                                    final org.litebridge.db.spi.Column column = aliasGenerator.aliasColumn(joinTable, joinColumn);
                                    return new org.litebridge.orm.expression.select.SelectFieldSpec(joinColumnField, column);
                                })
                                .filter(Objects::nonNull)
                                .toList();

                        dtoSelectSpec.addExpressions(joinFieldColumns);
                        joinSpec.setFieldColumns(joinFieldColumns.stream()
                                .map(selectField -> new DtoSelectSpec.FieldColumn(selectField.field(), selectField.getColumn()))
                                .toList());

                        dtoSelectSpec.addJoin(joinSpec);
                    }
                } else if (selectSpec instanceof SqlSelectSpec sqlSelectSpec && joinNode.tableName() != null) {
                    final SqlJoinSpec joinSpec = sqlSelectSpec.newJoinSpec(joinNode.tableName());
                }

                if (joinNode.condition() != null) {
                    final List<QueryNode> conditionNodes = flatten(joinNode.condition());

                    for (QueryNode queryNode : conditionNodes) {
                        applyNode(queryNode, joinNode, selectSpec);
                    }
                }

                if (selectSpec instanceof DtoSelectSpec && joinNode.dtoClass() != null) {
                    final Table joinTable = Objects.requireNonNull(selectSpec.getJoins()).get(selectSpec.getJoins().size() - 1).table();
                    aliasHistory.computeIfAbsent(joinNode.dtoClass(), k -> new ArrayList<>()).add(joinTable);
                }
            }
            case ConditionGroupNode conditionGroupNode -> {
                final QueryNode conditionGroupParentNode = Objects.requireNonNull(parentNode, "AST error: ConditionGroupNode outside of a parent context");

                // Create a new condition group spec on the relevant stack
                final ConditionGroupSpec conditionGroupSpec = switch (conditionGroupParentNode) {
                    case WhereNode whereNode -> selectSpec.pushWhereConditionGroup(conditionGroupNode.logicOperator());
                    case HavingNode havingNode -> selectSpec.pushHavingConditionGroup(conditionGroupNode.logicOperator());
                    case JoinNode joinNode -> {
                        final List<JoinSpec> joins = Objects.requireNonNull(selectSpec.getJoins());
                        final JoinSpec lastJoin = joins.get(joins.size() - 1);
                        yield lastJoin.pushConditionGroupSpec(conditionGroupNode.logicOperator());
                    }
                    default -> throw new IllegalStateException("AST error: Invalid condition context parent node: " + parentNode.getClass().getName());
                };

                final List<QueryNode> conditionGroupNodes = flatten(conditionGroupNode.lastChild());

                for (QueryNode queryNode : conditionGroupNodes) {
                    applyNode(queryNode, conditionGroupParentNode, selectSpec);
                }

                // Close the condition group spec on the relevant stack
                switch (conditionGroupParentNode) {
                    case WhereNode whereNode -> selectSpec.popWhereConditionGroup();
                    case HavingNode havingNode -> selectSpec.popHavingConditionGroup();
                    case JoinNode joinNode -> {
                        final List<JoinSpec> joins = Objects.requireNonNull(selectSpec.getJoins());
                        final JoinSpec lastJoin = joins.get(joins.size() - 1);
                        lastJoin.popConditionGroupSpec();
                    }
                    default -> throw new IllegalStateException("AST error: Invalid condition context parent node: " + parentNode.getClass().getName());
                }
            }
            case ConditionNode conditionNode -> {
                // Nested chains
                final ConditionGroupSpec conditionGroupSpec = switch (Objects.requireNonNull(parentNode, "AST error: Condition node outside of a parent context")) {
                    case WhereNode whereNode -> selectSpec.currentWhereConditionGroupSpec();
                    case HavingNode havingNode -> selectSpec.currentHavingConditionGroupSpec();
                    case JoinNode joinNode -> {
                        final List<JoinSpec> joins = Objects.requireNonNull(selectSpec.getJoins());
                        final JoinSpec lastJoin = joins.get(joins.size() - 1);

                        if (conditionNode.relationshipField() != null && lastJoin instanceof org.litebridge.orm.api.dto.DtoJoinSpec djs) {
                            // The source table is stored in the JoinNode
                            if (joinNode.sourceDtoClass() != null) {
                                final OrmTable sourceTable = tableRegistry.getTableOrThrow(joinNode.sourceDtoClass());
                                sourceTable.fieldAcessorStream()
                                        .filter(accessor -> accessor.name().equals(conditionNode.relationshipField()))
                                        .findFirst()
                                        .ifPresent(fieldAccessor -> {
                                            djs.setCollectionField(fieldAccessor);

                                            // Set reverse collection field if available
                                            djs.dtoTable().getOneToManyMappings().stream()
                                                    .filter(m -> m.mappedByField().equals(fieldAccessor))
                                                    .findFirst()
                                                    .ifPresent(m -> djs.setReverseCollectionField(m.collection()));

                                            djs.dtoTable().getManyToManyMappings().stream()
                                                    .filter(m -> m.joinTable().getMetaData().name().equals(sourceTable.getMetaData().name()))
                                                    .findFirst()
                                                    .ifPresent(m -> djs.setReverseCollectionField(m.collection()));
                                        });
                            }
                        }

                        if (conditionNode.operator() == Operator.USING) {
                            lastJoin.using(conditionNode.rhs().toString());
                        }

                        yield lastJoin.currentConditionGroupSpec();
                    }
                    default -> throw new IllegalStateException("AST error: Invalid condition context parent node: " + parentNode.getClass().getName());
                };

                if (conditionNode.operator() != Operator.USING) {
                    final Table sourceAlias;
                    final Table targetAlias;

                    if (parentNode instanceof JoinNode jn && jn.dtoClass() != null) {
                        final List<Table> history = aliasHistory.get(jn.sourceDtoClass());
                        sourceAlias = history != null && !history.isEmpty() ? history.get(history.size() - 1) : null;

                        final List<JoinSpec> joins = Objects.requireNonNull(selectSpec.getJoins());
                        targetAlias = joins.get(joins.size() - 1).table();
                    } else {
                        sourceAlias = null;
                        targetAlias = null;
                    }

                    final ExpressionSpec lhs = (ExpressionSpec) resolveAliases(conditionNode.lhs(), sourceAlias, targetAlias, true);
                    final Object rhs = resolveAliases(conditionNode.rhs(), sourceAlias, targetAlias, false);

                    final ConditionSpec conditionSpec = conditionGroupSpec.newCondition(conditionNode.logicOperator(), lhs);
                    conditionSpec.setOperator(conditionNode.operator());
                    conditionSpec.setValue(rhs);
                }
            }
            case WhereNode whereNode -> {
                final List<QueryNode> conditionNodes = flatten(whereNode.condition());

                for (QueryNode queryNode : conditionNodes) {
                    applyNode(queryNode, whereNode, selectSpec);
                }
            }
            case BeginGroupNode beginGroup -> selectSpec.pushWhereConditionGroup(beginGroup.logicOperator());
            case EndGroupNode endGroup -> selectSpec.popWhereConditionGroup();
            case GroupByNode groupByNode -> selectSpec.setGroupBy(List.of(groupByNode.expressions()));
            case HavingNode havingNode -> {
                final List<QueryNode> conditionNodes = flatten(havingNode.condition());

                for (QueryNode queryNode : conditionNodes) {
                    applyNode(queryNode, havingNode, selectSpec);
                }
            }
            case OrderByNode orderByNode -> selectSpec.addOrderBy(orderByNode.expression(), orderByNode.ascending());
            case LimitNode limitNode -> {
                if (limitNode.limit().isPresent()) {
                    selectSpec.ensureLimit().setLimit(limitNode.limit().get());
                }
                if (limitNode.offset().isPresent()) {
                    selectSpec.ensureLimit().setOffset(limitNode.offset().get());
                }
            }
        }
    }

    private static List<QueryNode> flatten(final QueryNode node) {
        final List<QueryNode> nodes = new ArrayList<>();
        QueryNode current = node;

        while (current != null) {
            nodes.add(current);
            current = current.previous();
        }

        Collections.reverse(nodes);
        return nodes;
    }

    private @Nullable Object resolveAliases(final @Nullable Object value, final @Nullable Table sourceAlias, final @Nullable Table targetAlias, boolean preferSource) {
        if (value == null) {
            return null;
        }

        if (value instanceof Column column) {
            OrmTable ormTable = tableRegistry.getTable(column.table());

            if (ormTable == null && sourceAlias != null) {
                final OrmTable sourceOrmTable = tableToOrmTableMap.get(sourceAlias);
                if (sourceOrmTable != null) {
                    ormTable = sourceOrmTable.getContextTableRegistry().getTable(column.table());
                }
            }

            if (ormTable == null && targetAlias != null) {
                final OrmTable targetOrmTable = tableToOrmTableMap.get(targetAlias);
                if (targetOrmTable != null) {
                    ormTable = targetOrmTable.getContextTableRegistry().getTable(column.table());
                }
            }

            if (ormTable != null) {
                Table resolvedTable = null;

                // If we have explicit source/target aliases for this join context, use them
                if (sourceAlias != null && ormTable.dtoClass().equals(getTableDtoClass(sourceAlias))) {
                    if (targetAlias != null && ormTable.dtoClass().equals(getTableDtoClass(targetAlias))) {
                        // Ambiguous (self-join); use preference
                        resolvedTable = preferSource ? sourceAlias : targetAlias;
                    } else {
                        resolvedTable = sourceAlias;
                    }
                } else if (targetAlias != null && ormTable.dtoClass().equals(getTableDtoClass(targetAlias))) {
                    resolvedTable = targetAlias;
                }

                if (resolvedTable == null) {
                    // Fallback to most recent alias in history
                    final List<Table> history = aliasHistory.get(ormTable.dtoClass());
                    if (history != null && !history.isEmpty()) {
                        resolvedTable = history.get(history.size() - 1);
                    }
                }

                if (resolvedTable != null) {
                    return new Column(resolvedTable, column.name(), column.alias());
                }
            }
        } else if (value instanceof ColumnExpressionSpec ces) {
            ces.setColumn((Column) resolveAliases(ces.getColumn(), sourceAlias, targetAlias, preferSource));
            return ces;
        }
        return value;
    }

    private @Nullable Class<?> getTableDtoClass(Table table) {
        final OrmTable ormTable = tableToOrmTableMap.getOrDefault(table, tableRegistry.getTable(table));
        return ormTable != null ? ormTable.dtoClass() : null;
    }
}
