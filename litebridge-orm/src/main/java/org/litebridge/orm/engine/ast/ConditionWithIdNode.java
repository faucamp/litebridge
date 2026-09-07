package org.litebridge.orm.engine.ast;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.query.Operator;

import java.util.Objects;

import static org.litebridge.orm.engine.ast.ConditionNodeUtil.valueStructuralKey;

/**
 * Query condition node for selecting a record by its primary key.
 *
 * @param previous      the previous node in the chain
 * @param logicOperator the logic operator (AND/OR)
 * @param operator      the operator (EQ, USING, etc.)
 * @param id            the ID/primary key value
 */
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
