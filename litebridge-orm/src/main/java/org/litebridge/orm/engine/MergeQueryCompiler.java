package org.litebridge.orm.engine;

import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.orm.api.select.SelectTerminal;
import org.litebridge.orm.api.select.ast.ConditionNode;
import org.litebridge.orm.api.select.ast.InsertNode;
import org.litebridge.orm.api.select.ast.InsertValuesNode;
import org.litebridge.orm.api.select.ast.MergeNode;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.ast.SetNode;
import org.litebridge.orm.api.select.ast.UpdateNode;
import org.litebridge.orm.api.select.ast.UsingNode;
import org.litebridge.orm.api.select.ast.WhenMatchedNode;
import org.litebridge.orm.api.select.ast.WhenNotMatchedNode;
import org.litebridge.orm.api.select.model.ConditionGroupSpec;
import org.litebridge.orm.api.select.model.ConditionSpec;
import org.litebridge.orm.api.select.model.SelectExpressionMapper;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.persistence.TableMetaDataCache;
import org.litebridge.orm.persistence.TableRegistry;
import org.litebridge.orm.persistence.alias.AliasGenerator;

import java.util.Objects;

final class MergeQueryCompiler extends ConditionBasedQueryCompiler<MergeCompilationContext> {

    public MergeQueryCompiler(final TableRegistry tableRegistry,
                              final TableMetaDataCache tableMetaDataCache,
                              final TypeConverter typeConverter,
                              final AliasGenerator aliasGenerator,
                              final SelectExpressionMapper selectExpressionMapper) {
        super(tableRegistry, tableMetaDataCache, typeConverter, aliasGenerator, selectExpressionMapper);
    }

    @Override
    MergeCompilationContext createCompilationContext(final QueryNode rootNode) {
        if (!(rootNode instanceof MergeNode mergeNode)) {
            throw new IllegalArgumentException("Expected MergeNode, but got: " + rootNode);
        }

        return new MergeCompilationContext(mergeNode, selectExpressionMapper, tableMetaDataCache, typeConverter);
    }

    @Override
    protected void applyNode(final QueryNode node, final MergeCompilationContext compilationContext) {
        switch (node) {
            case InsertNode insertNode -> compilationContext.whenNotMatchedInsert(insertNode);
            case InsertValuesNode insertValuesNode -> compilationContext.addBindValues(insertValuesNode.values());
            case UsingNode usingNode -> {
                compilationContext.setUsingNode(usingNode);
                flattenAndApplyNodes(usingNode.on(), compilationContext);
            }
            case WhenMatchedNode whenMatchedNode -> {
                final MergeCompilationContext.WhenMatchedSpec whenMatchedSpec = compilationContext.addWhenMatchedSpec(true);

                // "AND" condition
                if (whenMatchedNode.and() != null) {
                    flattenAndApplyNodes(whenMatchedNode.and(), compilationContext);
                }

                if (whenMatchedNode.update() != null) {
                    flattenAndApplyNodes(whenMatchedNode.update(), compilationContext);
                } else if (whenMatchedNode.delete()) {
                    whenMatchedSpec.setDelete(true);
                }
            }
            case WhenNotMatchedNode whenNotMatchedNode -> {
                compilationContext.addWhenMatchedSpec(false);
                flattenAndApplyNodes(whenNotMatchedNode.insert(), compilationContext);
            }
            case ConditionNode conditionNode -> {
                // Nested chains
                final ConditionGroupSpec conditionGroupSpec = compilationContext.getConditionGroupSpecStack().current();

                final Table sourceAlias;
                final Table targetAlias;
                sourceAlias = null;
                targetAlias = null;


                final ExpressionSpec lhs = (ExpressionSpec) resolveAliases(conditionNode.lhs(), sourceAlias, targetAlias, true);
                final Object rhsValue = resolveAliases(conditionNode.rhs(), sourceAlias, targetAlias, false);
                final Object rhs;

                if (rhsValue instanceof SelectTerminal<?> st) {
                    rhs = createSelectSpec(st);
                } else {
                    rhs = rhsValue;

                    if (!(rhsValue instanceof ExpressionSpec)) {
                        compilationContext.addBindValues(rhsValue);
                    }
                }

                final ConditionSpec conditionSpec = conditionGroupSpec.newCondition(conditionNode.logicOperator(), Objects.requireNonNull(lhs));
                conditionSpec.setOperator(conditionNode.operator());
                conditionSpec.setValue(rhs);

            }
            case MergeNode mergeNode -> {
                // Ignore
            }
            case UpdateNode updateNode -> {
                // Ignore
            }
            case SetNode setNode -> compilationContext.whenMatchedUpdateSet(setNode);
            default -> throw new UnsupportedOperationException("Unsupported node type: " + node.getClass().getName());
        }
    }
}
