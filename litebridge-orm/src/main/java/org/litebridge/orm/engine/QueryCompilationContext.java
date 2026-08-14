package org.litebridge.orm.engine;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Operation;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.orm.api.select.model.ConditionGroupSpec;

import java.util.List;
import java.util.Objects;

final class QueryCompilationContext implements CompilationContext {

    private @Nullable CompilationContext compilationContext;
    private @Nullable MergeCompilationContext mergeCompilationContext;

    public void setCompilationContext(final CompilationContext compilationContext) {
        this.compilationContext = compilationContext;
    }

    public InsertCompilationContext getInsertCompilationContext() {
        return (InsertCompilationContext) Objects.requireNonNull(compilationContext);
    }

    public MergeCompilationContext getMergeCompilationContext() {
        return (MergeCompilationContext) Objects.requireNonNull(compilationContext);
    }

    @Override
    public List<BindValue> getBindValues() {
        return Objects.requireNonNull(compilationContext).getBindValues();
    }

    @Override
    public Operation toOperation() {
        return Objects.requireNonNull(compilationContext).toOperation();
    }

    @Override
    public ConditionGroupSpec getConditionGroupSpec() {
        return Objects.requireNonNull(compilationContext).getConditionGroupSpec();
    }
}
