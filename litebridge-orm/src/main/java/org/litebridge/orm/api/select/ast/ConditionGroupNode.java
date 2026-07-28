package org.litebridge.orm.api.select.ast;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.LogicOperator;

public record ConditionGroupNode(@Nullable QueryNode previous,
                                 LogicOperator logicOperator,
                                 QueryNode lastChild) implements ConditionQueryNode {
}
