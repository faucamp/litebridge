package org.litebridge.orm.engine.compiler;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.Join;
import org.litebridge.db.spi.query.SelectTarget;
import org.litebridge.orm.engine.ast.ConditionJoinUsingNode;
import org.litebridge.orm.engine.ast.JoinNode;

import java.util.Objects;

final class JoinSpec {

    private final @Nullable JoinNode joinNode;
    private final ConditionGroupSpecStack conditionGroupSpecStack = new ConditionGroupSpecStack();
    private @Nullable ConditionJoinUsingNode conditionJoinUsingNode;
    private @Nullable SelectTarget joinTarget;
    private @Nullable SelectTarget sourceTarget;

    JoinSpec(final JoinNode joinNode) {
        this.joinNode = joinNode;
    }

    JoinSpec() {
        this.joinNode = null;
    }

    JoinNode joinNode() {
        return Objects.requireNonNull(joinNode);
    }

    ConditionGroupSpecStack conditionGroupStack() {
        return conditionGroupSpecStack;
    }

    public @Nullable ConditionJoinUsingNode getConditionJoinUsingNode() {
        return conditionJoinUsingNode;
    }

    public void setConditionJoinUsingNode(@Nullable final ConditionJoinUsingNode conditionJoinUsingNode) {
        this.conditionJoinUsingNode = conditionJoinUsingNode;
    }

    public Join.JoinType type() {
        return joinNode().type();
    }

    public SelectTarget getJoinTarget() {
        return Objects.requireNonNull(joinTarget);
    }

    public void setJoinTarget(@Nullable final SelectTarget joinTarget) {
        this.joinTarget = joinTarget;
    }

    public @Nullable SelectTarget getSourceTarget() {
        return sourceTarget;
    }

    public void setSourceTarget(@Nullable final SelectTarget sourceTarget) {
        this.sourceTarget = sourceTarget;
    }
}
