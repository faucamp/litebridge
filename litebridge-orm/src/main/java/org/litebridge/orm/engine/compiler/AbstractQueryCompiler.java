package org.litebridge.orm.engine.compiler;

import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.model.SelectExpressionMapper;
import org.litebridge.orm.persistence.TableMetaDataCache;
import org.litebridge.orm.persistence.TableRegistry;
import org.litebridge.orm.persistence.alias.AliasGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

abstract sealed class AbstractQueryCompiler<CC extends CompilationContext>
        permits ConditionBasedQueryCompiler, DeleteQueryCompiler, InsertQueryCompiler, QueryCompiler, UpdateQueryCompiler {

    protected final TableRegistry tableRegistry;
    protected final TableMetaDataCache tableMetaDataCache;
    protected final TypeConverter typeConverter;
    protected final AliasGenerator aliasGenerator;
    protected final SelectExpressionMapper selectExpressionMapper;

    public AbstractQueryCompiler(final TableRegistry tableRegistry,
                                 final TableMetaDataCache tableMetaDataCache,
                                 final TypeConverter typeConverter,
                                 final AliasGenerator aliasGenerator,
                                 final SelectExpressionMapper selectExpressionMapper) {
        this.tableRegistry = tableRegistry;
        this.tableMetaDataCache = tableMetaDataCache;
        this.typeConverter = typeConverter;
        this.aliasGenerator = aliasGenerator;
        this.selectExpressionMapper = selectExpressionMapper;
    }

    @SuppressWarnings("unchecked")
    final void applyNodes(final List<QueryNode> nodes, final CompilationContext compilationContext) {
        final CC castedCompilationContext = (CC) compilationContext;

        for (final QueryNode node : nodes) {
            applyNode(node, castedCompilationContext);
        }
    }

    abstract CC createCompilationContext(QueryNode rootNode);

    protected abstract void applyNode(final QueryNode node, final CC compilationContext);

    protected final void flattenAndApplyNodes(final QueryNode terminalNode, final CC compilationContext) {
        final List<QueryNode> nodes = flatten(terminalNode);
        applyNodes(nodes, compilationContext);
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
}
