package org.litebridge.db.oracle.api.insert;

import org.litebridge.orm.api.insert.DtoInsertIntoStep;
import org.litebridge.orm.api.insert.InsertValuesStep;
import org.litebridge.orm.api.insert.InsertValuesStepInspector;
import org.litebridge.orm.api.insert.SqlInsertIntoStep;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.InsertValuesNode;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class InsertAllStep {

    private final Function<LitebridgeContext.Mode, LitebridgeContext> litebridgeContextCreator;
    private final List<InsertValuesNode> insertValuesNodes = new ArrayList<>();

    public InsertAllStep(final Function<LitebridgeContext.Mode, LitebridgeContext> litebridgeContextCreator) {
        this.litebridgeContextCreator = litebridgeContextCreator;
    }

    public InsertAllStep intoTable(final Class<?> dtoClass, Function<DtoInsertIntoStep, InsertValuesStep> insert) {
        final DtoInsertIntoStep dtoInsertIntoStep = new DtoInsertIntoStep(dtoClass, litebridgeContextCreator.apply(LitebridgeContext.Mode.SQL));
        final InsertValuesStep terminal = insert.apply(dtoInsertIntoStep);
        final InsertValuesNode insertValuesNode = (InsertValuesNode) InsertValuesStepInspector.getNode(terminal);
        insertValuesNodes.add(insertValuesNode);
        return this;
    }

    public InsertAllStep intoTable(final String table, Function<SqlInsertIntoStep, InsertValuesStep> insert) {
        final SqlInsertIntoStep sqlInsertIntoStep = new SqlInsertIntoStep(table, litebridgeContextCreator.apply(LitebridgeContext.Mode.SQL));
        final InsertValuesStep terminal = insert.apply(sqlInsertIntoStep);
        final InsertValuesNode insertValuesNode = (InsertValuesNode) InsertValuesStepInspector.getNode(terminal);
        insertValuesNodes.add(insertValuesNode);
        return this;
    }

    List<InsertValuesNode> insertValuesNodes() {
        return insertValuesNodes;
    }
}
