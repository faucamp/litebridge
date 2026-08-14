package org.litebridge.orm.api.merge;

import org.litebridge.db.spi.Table;
import org.litebridge.orm.api.insert.InsertIntoStep;
import org.litebridge.orm.api.insert.InsertValuesStep;
import org.litebridge.orm.api.insert.InsertValuesStepInspector;
import org.litebridge.orm.api.select.ast.InsertNode;
import org.litebridge.orm.api.select.ast.InsertValuesNode;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.ast.WhenMatchedNode;
import org.litebridge.orm.api.select.ast.WhenNotMatchedNode;
import org.litebridge.orm.engine.LitebridgeContext;

import java.util.function.Function;

public class MergeWhenNotMatchedStep<DTO> extends MergeTerminal {

    private final Table table;
    private final LitebridgeContext litebridgeContext;

    public MergeWhenNotMatchedStep(final Table table, final QueryNode node, final LitebridgeContext litebridgeContext) {
        super(node);
        this.table = table;
        this.litebridgeContext = litebridgeContext;
    }

    public MergeTerminal whenNotMatched(final Function<MergeInsertStep, InsertValuesStep> insert) {
        final MergeInsertStep mergeInsertStep = new MergeInsertStep(table, node, litebridgeContext);
        final InsertValuesStep insertValuesStep = insert.apply(mergeInsertStep);
        final QueryNode terminalNode = InsertValuesStepInspector.getNode(insertValuesStep);
        return new MergeTerminal(new WhenNotMatchedNode(node, (InsertValuesNode) terminalNode));
    }
}
