package org.litebridge.orm.engine.compiler;

import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.orm.api.select.ast.ConditionGroupNode;
import org.litebridge.orm.api.select.ast.ConditionNode;
import org.litebridge.orm.api.select.ast.DeleteNode;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.ast.WhereNode;
import org.litebridge.orm.api.select.model.SelectExpressionMapper;
import org.litebridge.orm.persistence.TableMetaDataCache;
import org.litebridge.orm.persistence.TableRegistry;
import org.litebridge.orm.persistence.alias.AliasGenerator;

final class DeleteQueryCompiler extends AbstractQueryCompiler<DeleteCompilationContext> {

    public DeleteQueryCompiler(final TableRegistry tableRegistry,
                               final TableMetaDataCache tableMetaDataCache,
                               final TypeConverter typeConverter,
                               final AliasGenerator aliasGenerator,
                               final SelectExpressionMapper selectExpressionMapper) {
        super(tableRegistry, tableMetaDataCache, typeConverter, aliasGenerator, selectExpressionMapper);
    }

    @Override
    DeleteCompilationContext createCompilationContext(final QueryNode rootNode) {
        if (!(rootNode instanceof DeleteNode deleteNode)) {
            throw new IllegalArgumentException("Expected DeleteNode, but got " + rootNode);
        }

        return new DeleteCompilationContext(deleteNode, selectExpressionMapper, tableRegistry, tableMetaDataCache, typeConverter);
    }

    @Override
    protected void applyNode(final QueryNode node, final DeleteCompilationContext compilationContext) {
        switch (node) {
            case WhereNode whereNode -> flattenAndApplyNodes(whereNode.condition(), compilationContext);
            case DeleteNode deleteNode -> { /* Ignore */ }
            case ConditionNode conditionNode -> compilationContext.addWhereCondition(conditionNode);
            case ConditionGroupNode conditionGroupNode -> {
                final ConditionGroupSpecStack conditionGroupSpecStack = compilationContext.ensureWhereConditionGroupStack();
                conditionGroupSpecStack.push(conditionGroupNode.logicOperator());
                flattenAndApplyNodes(conditionGroupNode.lastChild(), compilationContext);
                conditionGroupSpecStack.pop();
            }
            default -> throw new UnsupportedOperationException("Unsupported node type: " + node.getClass().getName());
        }
    }
}
