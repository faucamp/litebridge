package org.litebridge.orm.engine;

import org.litebridge.db.spi.Operation;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.orm.api.select.model.ConditionGroupSpec;

import java.util.List;

interface CompilationContext {

    List<BindValue> getBindValues();

    Operation toOperation();

    ConditionGroupSpec getConditionGroupSpec();
}
