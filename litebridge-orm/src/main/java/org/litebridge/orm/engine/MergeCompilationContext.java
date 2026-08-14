package org.litebridge.orm.engine;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Operation;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.db.spi.update.Merge;
import org.litebridge.orm.api.select.ast.MergeNode;
import org.litebridge.orm.api.select.ast.UsingNode;
import org.litebridge.orm.api.select.ast.WhenMatchedNode;
import org.litebridge.orm.api.select.ast.WhenNotMatchedNode;
import org.litebridge.orm.api.select.model.ConditionGroupSpec;
import org.litebridge.orm.api.select.model.SelectExpressionMapper;
import org.litebridge.orm.api.sql.SqlProtoExpressionResolver;
import org.litebridge.orm.persistence.TableMetaDataCache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class MergeCompilationContext implements CompilationContext {

    private final MergeNode mergeNode;
    private final SelectExpressionMapper selectExpressionMapper;
    private final TableMetaDataCache tableMetaDataCache;
    private final TypeConverter typeConverter;
    private @Nullable ConditionGroupSpec on;
    private @Nullable UsingNode usingNode;
    private @Nullable List<WhenMatchedNode> whenMatchedNodes;
    private @Nullable List<WhenNotMatchedNode> whenNotMatchedNodes;

    public MergeCompilationContext(final MergeNode mergeNode,
                                   final SelectExpressionMapper selectExpressionMapper,
                                   final TableMetaDataCache tableMetaDataCache,
                                   final TypeConverter typeConverter) {
        this.mergeNode = mergeNode;
        this.selectExpressionMapper = selectExpressionMapper;
        this.tableMetaDataCache = tableMetaDataCache;
        this.typeConverter = typeConverter;
    }

    public void setUsingNode(UsingNode usingNode) {
        this.usingNode = usingNode;
    }

    public void addWhenMatchedNode(final WhenMatchedNode whenMatchedNode) {
        if (whenMatchedNodes == null) {
            whenMatchedNodes = new ArrayList<>();
        }

        whenMatchedNodes.add(whenMatchedNode);
    }

    public void addWhenNotMatchedNode(final WhenNotMatchedNode whenNotMatchedNode) {
        if (whenNotMatchedNodes == null) {
            whenNotMatchedNodes = new ArrayList<>();
        }

        whenNotMatchedNodes.add(whenNotMatchedNode);
    }

    @Override
    public List<BindValue> getBindValues() {
        return List.of();
    }

    @Override
    public Operation toOperation() {
        final UsingNode usingNode = Objects.requireNonNull(this.usingNode);
        final Table usingTable = usingNode.table();
        final List<BindValue> bindValues = new ArrayList<>();

        return new Merge(mergeNode.table(),
                usingTable,
                null,
                on.toConditionGroup(selectExpressionMapper,
                        Set.of(mergeNode.table(), usingTable),
                        bindValues,
                        tableMetaDataCache,
                        typeConverter),
                Collections.emptyList(),
                null);
    }

    @Override
    public ConditionGroupSpec getConditionGroupSpec() {
        if (on == null) {
            on = new ConditionGroupSpec();
        }

        return on;
    }
}
