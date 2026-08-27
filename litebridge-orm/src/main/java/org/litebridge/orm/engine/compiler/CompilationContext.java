package org.litebridge.orm.engine.compiler;

import org.litebridge.db.spi.Operation;
import org.litebridge.db.spi.sql.BindValue;

import java.util.List;

sealed interface CompilationContext permits AbstractCompilationContext, InsertCompilationContext, MergeCompilationContext {

    List<BindValue> getBindValues();

    Operation toOperation();
}
