package org.litebridge.orm.engine;

import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.orm.api.select.ast.InsertNode;
import org.litebridge.orm.api.select.ast.InsertValuesNode;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.model.SelectExpressionMapper;
import org.litebridge.orm.persistence.TableMetaDataCache;
import org.litebridge.orm.persistence.TableRegistry;
import org.litebridge.orm.persistence.alias.AliasGenerator;

import java.util.Arrays;

final class InsertQueryCompiler extends AbstractQueryCompiler<InsertCompilationContext> {

    public InsertQueryCompiler(final TableRegistry tableRegistry,
                               final TableMetaDataCache tableMetaDataCache,
                               final TypeConverter typeConverter,
                               final AliasGenerator aliasGenerator,
                               final SelectExpressionMapper selectExpressionMapper) {
        super(tableRegistry, tableMetaDataCache, typeConverter, aliasGenerator, selectExpressionMapper);
    }

    @Override
    InsertCompilationContext createCompilationContext(final QueryNode rootNode) {
        if (!(rootNode instanceof InsertNode insertNode)) {
            throw new IllegalArgumentException("Expected InsertNode, but got " + rootNode);
        }

        return new InsertCompilationContext(insertNode, tableRegistry, tableMetaDataCache, typeConverter);
    }

    @Override
    protected void applyNode(final QueryNode node, final InsertCompilationContext compilationContext) {
        switch (node) {
            case InsertValuesNode insertValuesNode ->
                    compilationContext.addRowBindValues(Arrays.asList(insertValuesNode.values()));
            case InsertNode insertNode -> { /* Ignore */ }
            default -> throw new UnsupportedOperationException("Unsupported node type: " + node.getClass().getName());
        }
    }
}
