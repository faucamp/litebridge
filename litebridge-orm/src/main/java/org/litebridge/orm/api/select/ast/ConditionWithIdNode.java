package org.litebridge.orm.api.select.ast;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.query.Operator;

import java.util.Objects;

import static org.litebridge.orm.api.select.ast.ConditionNodeUtil.valueStructuralKey;

public record ConditionWithIdNode(@Nullable QueryNode previous,
                                  LogicOperator logicOperator,
                                  Operator operator,
                                  @Nullable Object id) implements ConditionQueryNode {

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof final ConditionWithIdNode that)) return false;
        return operator == that.operator
                && Objects.equals(previous, that.previous)
                && logicOperator == that.logicOperator
                && Objects.equals(valueStructuralKey(id), valueStructuralKey(that.id));
    }

    @Override
    public int hashCode() {
        return Objects.hash(previous, logicOperator, operator, operator, valueStructuralKey(id));
    }
}
