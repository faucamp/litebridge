package org.litebridgedb.orm.expression.select;

import org.litebridgedb.orm.api.select.model.SelectSpec;
import org.litebridgedb.orm.expression.ExpressionSpec;

public record SubselectSpec(SelectSpec selectSpec) implements ExpressionSpec {

}
