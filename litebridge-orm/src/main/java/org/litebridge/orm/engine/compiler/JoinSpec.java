package org.litebridge.orm.engine.compiler;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.Join;
import org.litebridge.orm.engine.ast.ConditionJoinUsingNode;
import org.litebridge.orm.engine.ast.JoinNode;

import java.util.Objects;

final class JoinSpec {

    private final @Nullable JoinNode joinNode;
    private final ConditionGroupSpecStack conditionGroupSpecStack = new ConditionGroupSpecStack();
    private @Nullable ConditionJoinUsingNode conditionJoinUsingNode;

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
}
