package org.litebridge.orm.engine.compiler;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.query.ConditionGroup;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.db.spi.update.Delete;
import org.litebridge.orm.api.select.ast.ConditionNode;
import org.litebridge.orm.api.select.ast.DeleteNode;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.persistence.OrmTable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class DeleteCompilationContext extends AbstractCompilationContext {

    private static final ConditionGroup EMPTY_CONDITION_GROUP = new ConditionGroup(Collections.emptyList());

    private final Table table;
    private final @Nullable OrmTable ormTable;
    private @Nullable ConditionGroupSpecStack where;

    public DeleteCompilationContext(final DeleteNode deleteNode,
                                    final LitebridgeContext litebridgeContext) {
        super(litebridgeContext);

        if (deleteNode.dtoClass() != null) {
            this.ormTable = litebridgeContext.tableRegistry().getTableOrThrow(deleteNode.dtoClass());
            this.table = ormTable.getMetaData().toTable();
        } else {
            this.ormTable = null;
            this.table = litebridgeContext.tableRegistry().getOrCreateSpiTable(Objects.requireNonNull(deleteNode.table()));
        }
    }

    public ConditionGroupSpecStack ensureWhereConditionGroupStack() {
        if (where == null) {
            where = new ConditionGroupSpecStack();
        }

        return where;
    }

    public void addWhereCondition(final ConditionNode conditionNode) {
        ensureWhereConditionGroupStack().current().newCondition(conditionNode.logicOperator(),
                conditionNode.lhsColumn(),
                conditionNode.lhsExpression(),
                conditionNode.operator(),
                conditionNode.rhs());
    }

    @Override
    public List<BindValue> getBindValues() {
        return bindValues;
    }

    @Override
    public Delete toOperation() {
        final ConditionGroup whereConditionGroup;

        if (where != null) {
            whereConditionGroup = toConditionGroup(where.current(), ormTable, table);
        } else {
            whereConditionGroup = EMPTY_CONDITION_GROUP;
        }

        return new Delete(table, whereConditionGroup);
    }
}
