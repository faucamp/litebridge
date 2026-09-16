package org.litebridge.orm.api.merge;

import org.jspecify.annotations.Nullable;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.MergeNode;
import org.litebridge.orm.engine.ast.QueryNode;

abstract sealed class MergeStepBase permits MergeAndStep, MergeOnStep {

    /**
     * The root merge node containing target table information
     */
    protected final MergeNode mergeNode;
    /**
     * The using table name in SQL mode.
     */
    protected final @Nullable String usingTable;
    /**
     * The using DTO class in DTO mode.
     */
    protected final @Nullable Class<?> usingDtoClass;
    /**
     * The using sub-query's terminal node
     */
    protected final @Nullable QueryNode usingSubselectNode;
    /**
     * The Litebridge context.
     */
    protected final LitebridgeContext litebridgeContext;

    MergeStepBase(final String usingTable,
                  final MergeNode mergeNode,
                  final LitebridgeContext litebridgeContext) {
        this.mergeNode = mergeNode;
        this.usingTable = usingTable;
        this.usingDtoClass = null;
        this.usingSubselectNode = null;
        this.litebridgeContext = litebridgeContext;
    }

    MergeStepBase(final Class<?> usingDtoClass,
                  final MergeNode mergeNode,
                  final LitebridgeContext litebridgeContext) {
        this.mergeNode = mergeNode;
        this.usingTable = null;
        this.usingDtoClass = usingDtoClass;
        this.usingSubselectNode = null;
        this.litebridgeContext = litebridgeContext;
    }

    MergeStepBase(final QueryNode usingSubselectNode,
                  final MergeNode mergeNode,
                  final LitebridgeContext litebridgeContext) {
        this.mergeNode = mergeNode;
        this.usingTable = null;
        this.usingDtoClass = null;
        this.usingSubselectNode = usingSubselectNode;
        this.litebridgeContext = litebridgeContext;
    }
}
