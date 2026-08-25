package org.litebridge.orm.engine.compiler;

import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.orm.api.select.ast.ConditionGroupNode;
import org.litebridge.orm.api.select.ast.ConditionNode;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.ast.SetNode;
import org.litebridge.orm.api.select.ast.UpdateNode;
import org.litebridge.orm.api.select.ast.WhereNode;
import org.litebridge.orm.api.select.model.SelectExpressionMapper;
import org.litebridge.orm.persistence.TableMetaDataCache;
import org.litebridge.orm.persistence.TableRegistry;
import org.litebridge.orm.persistence.alias.AliasGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class UpdateQueryCompiler extends AbstractQueryCompiler<UpdateCompilationContext> {

    public UpdateQueryCompiler(final TableRegistry tableRegistry,
                               final TableMetaDataCache tableMetaDataCache,
                               final TypeConverter typeConverter,
                               final AliasGenerator aliasGenerator,
                               final SelectExpressionMapper selectExpressionMapper) {
        super(tableRegistry, tableMetaDataCache, typeConverter, aliasGenerator, selectExpressionMapper);
    }

    @Override
    UpdateCompilationContext createCompilationContext(final QueryNode rootNode) {
        if (!(rootNode instanceof UpdateNode updateNode)) {
            throw new IllegalArgumentException("Expected UpdateNode, but got " + rootNode);
        }

        return new UpdateCompilationContext(updateNode, selectExpressionMapper, tableRegistry, tableMetaDataCache, typeConverter);
    }

    @Override
    protected void applyNode(final QueryNode node, final UpdateCompilationContext compilationContext) {
        switch (node) {
            case SetNode setNode -> compilationContext.addSetNode(setNode);
            case WhereNode whereNode -> flattenAndApplyNodes(whereNode.condition(), compilationContext);
            case UpdateNode updateNode -> { /* Ignore */ }
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
