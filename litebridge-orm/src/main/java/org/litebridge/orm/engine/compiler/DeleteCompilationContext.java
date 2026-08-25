package org.litebridge.orm.engine.compiler;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.query.ConditionGroup;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.db.spi.update.Delete;
import org.litebridge.orm.api.select.ast.ConditionNode;
import org.litebridge.orm.api.select.ast.DeleteNode;
import org.litebridge.orm.api.select.model.SelectExpressionMapper;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.orm.persistence.TableMetaDataCache;
import org.litebridge.orm.persistence.TableRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class DeleteCompilationContext implements CompilationContext {

    private static final ConditionGroup EMPTY_CONDITION_GROUP = new ConditionGroup(Collections.emptyList());

    private final Table table;
    private final SelectExpressionMapper selectExpressionMapper;
    private final TableMetaDataCache tableMetaDataCache;
    private final TableMetaData tableMetaData;
    private final TypeConverter typeConverter;
    private final List<BindValue> bindValues = new ArrayList<>();
    private @Nullable ConditionGroupSpecStack where;

    public DeleteCompilationContext(final DeleteNode deleteNode,
                                    final SelectExpressionMapper selectExpressionMapper,
                                    final TableRegistry tableRegistry,
                                    final TableMetaDataCache tableMetaDataCache,
                                    final TypeConverter typeConverter) {
        this.selectExpressionMapper = selectExpressionMapper;
        this.tableMetaDataCache = tableMetaDataCache;

        if (deleteNode.dtoClass() != null) {
            final OrmTable ormTable = tableRegistry.getTableOrThrow(deleteNode.dtoClass());
            this.tableMetaData = ormTable.getMetaData();
            this.table = tableMetaData.toTable();
        } else {
            this.table = tableRegistry.getOrCreateSpiTable(Objects.requireNonNull(deleteNode.table()));
            this.tableMetaData = tableMetaDataCache.ensureTableMetaData(table);
        }

        this.typeConverter = typeConverter;
    }

    public ConditionGroupSpecStack ensureWhereConditionGroupStack() {
        if (where == null) {
            where = new ConditionGroupSpecStack();
        }

        return where;
    }

    public void addWhereCondition(final ConditionNode conditionNode) {
        ensureWhereConditionGroupStack().current().newCondition(conditionNode.logicOperator(), conditionNode.lhs(), conditionNode.operator());
        //TODO: fix datatype
        final int sqlDataType = 0;
        final BindValue bindValue = new BindValue(conditionNode.rhs(), sqlDataType);
        bindValues.add(bindValue);
    }

    @Override
    public List<BindValue> getBindValues() {
        return bindValues;
    }

    @Override
    public Delete toOperation() {
        final ConditionGroup whereConditionGroup;

        if (where != null) {
            whereConditionGroup = where.current().toConditionGroup(selectExpressionMapper,
                    Set.of(table),
                    //TODO: temp patch until condition spec stuff is replaced
                    new ArrayList<>(),
                    tableMetaDataCache,
                    typeConverter);
        } else {
            whereConditionGroup = EMPTY_CONDITION_GROUP;
        }

        return new Delete(table, whereConditionGroup);
    }
}
