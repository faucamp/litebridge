package org.litebridge.orm.engine.compiler;

import org.litebridge.db.spi.Operation;
import org.litebridge.db.spi.sql.BindValue;

import java.util.List;

interface CompilationContext {

    List<BindValue> getBindValues();

    Operation toOperation();
}
