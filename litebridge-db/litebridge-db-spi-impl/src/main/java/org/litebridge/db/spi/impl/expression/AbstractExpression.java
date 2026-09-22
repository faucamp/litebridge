package org.litebridge.db.spi.impl.expression;

import org.litebridge.db.spi.expression.SelectExpression;
import org.litebridge.db.spi.impl.sql.LabelGenerator;

public abstract class AbstractExpression implements SelectExpression {

    protected final LabelGenerator labelGenerator;

    protected AbstractExpression(final LabelGenerator labelGenerator) {
        this.labelGenerator = labelGenerator;
    }
}
