package org.litebridge.orm.engine;

import org.litebridge.db.spi.Table;
import org.litebridge.orm.api.dto.DtoSelectSpec;
import org.litebridge.orm.api.select.ast.*;
import org.litebridge.orm.api.select.model.ConditionGroupSpec;
import org.litebridge.orm.api.select.model.ConditionSpec;
import org.litebridge.orm.api.select.model.JoinSpec;
import org.litebridge.orm.api.select.model.SelectSpec;
import org.litebridge.orm.api.sql.SqlSelectSpec;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.orm.persistence.TableRegistry;
import org.litebridge.orm.persistence.alias.AliasGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Compiles a {@link QueryNode} chain into a {@link SelectSpec}.
 */
public final class QueryCompiler {

    private final TableRegistry tableRegistry;
    private final AliasGenerator aliasGenerator;

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
        final List<QueryNode> nodes = flatten(node);
        for (final QueryNode n : nodes) {
            applyNode(n, selectSpec);
        }
    }

    private List<QueryNode> flatten(final QueryNode node) {
        final List<QueryNode> nodes = new ArrayList<>();
        QueryNode current = node;
        while (current != null) {
            nodes.add(current);
            current = current.previous();
        }
        Collections.reverse(nodes);
        return nodes;
    }

    private void applyNode(final QueryNode node, final SelectSpec selectSpec) {
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
                    final OrmTable joinOrmTable = tableRegistry.getTableOrThrow(joinNode.dtoClass());
                    final Table joinTable = aliasGenerator.aliasTable(joinOrmTable);
                    final org.litebridge.orm.api.dto.DtoJoinSpec joinSpec = new org.litebridge.orm.api.dto.DtoJoinSpec(
                            joinNode.dtoClass(),
                            joinOrmTable,
                            joinTable,
                            dtoSelectSpec.getSelectExpressionMapper());
                    joinSpec.setSourceDtoClass(joinNode.sourceDtoClass());

                    // Extend selects with joined table columns
                    final List<org.litebridge.orm.expression.select.SelectFieldSpec> joinFieldColumns = joinOrmTable.getMetaData().columns().stream()
                            .map(joinColumn -> {
                                final org.litebridge.tracking.FieldAccessor joinColumnField = joinOrmTable.getFieldForColumnName(joinColumn.name());
                                final org.litebridge.db.spi.Column column = aliasGenerator.aliasColumn(joinTable, joinColumn);
                                return new org.litebridge.orm.expression.select.SelectFieldSpec(joinColumnField, column);
                            })
                            .toList();

                    dtoSelectSpec.addExpressions(joinFieldColumns);
                    joinSpec.setFieldColumns(joinFieldColumns.stream()
                            .map(selectField -> new DtoSelectSpec.FieldColumn(selectField.field(), selectField.getColumn()))
                            .toList());

                    dtoSelectSpec.addJoin(joinSpec);
                } else if (selectSpec instanceof SqlSelectSpec sqlSelectSpec && joinNode.tableName() != null) {
                    final org.litebridge.orm.api.sql.SqlJoinSpec joinSpec = sqlSelectSpec.newJoinSpec(joinNode.tableName());
                }
            }
            case JoinConditionNode condNode -> {
                final List<JoinSpec> joins = selectSpec.getJoins();
                if (joins != null && !joins.isEmpty()) {
                    final JoinSpec lastJoin = joins.get(joins.size() - 1);

                    if (condNode.relationshipField() != null && lastJoin instanceof org.litebridge.orm.api.dto.DtoJoinSpec djs) {
                        // The source table is stored in the JoinNode
                        final QueryNode previous = condNode.previous();
                        if (previous instanceof JoinNode joinNode && joinNode.sourceDtoClass() != null) {
                            final OrmTable sourceTable = tableRegistry.getTableOrThrow(joinNode.sourceDtoClass());
                            sourceTable.fieldAcessorStream()
                                    .filter(accessor -> accessor.name().equals(condNode.relationshipField()))
                                    .findFirst()
                                    .ifPresent(fieldAccessor -> {
                                        djs.setCollectionField(fieldAccessor);

                                        // Set reverse collection field if available
                                        djs.dtoTable().getOneToManyMappings().stream()
                                                .filter(m -> m.mappedByField().equals(fieldAccessor))
                                                .findFirst()
                                                .ifPresent(m -> djs.setReverseCollectionField(m.collection()));
                                    });
                        }
                    }

                    if (condNode.operator() == org.litebridge.db.spi.query.Operator.USING) {
                        lastJoin.using(condNode.rhs().toString());
                    } else {
                        final ConditionSpec condition = lastJoin.currentConditionGroupSpec().newCondition(condNode.logicOperator(), condNode.lhs());
                        condition.setOperator(condNode.operator());
                        condition.setValue(condNode.rhs());
                    }
                }
            }
            case WhereNode whereNode -> {
                final ConditionSpec condition = selectSpec.currentWhereConditionGroupSpec().newCondition(whereNode.logicOperator(), whereNode.lhs());
                condition.setOperator(whereNode.operator());
                condition.setValue(whereNode.rhs());
            }
            case BeginGroupNode beginGroup -> selectSpec.pushWhereConditionGroup(beginGroup.logicOperator());
            case EndGroupNode endGroup -> selectSpec.popWhereConditionGroup();
            case GroupByNode groupByNode -> selectSpec.setGroupBy(List.of(groupByNode.expressions()));
            case HavingNode havingNode -> {
                final ConditionSpec condition = selectSpec.currentHavingConditionGroupSpec().newCondition(havingNode.logicOperator(), havingNode.lhs());
                condition.setOperator(havingNode.operator());
                condition.setValue(havingNode.rhs());
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
}
