package org.litebridge.orm.expression.select;

import org.litebridge.orm.api.select.model.SelectSpec;
import org.litebridge.orm.expression.ExpressionSpec;

public record SubselectSpec(SelectSpec selectSpec) implements ExpressionSpec {

}
