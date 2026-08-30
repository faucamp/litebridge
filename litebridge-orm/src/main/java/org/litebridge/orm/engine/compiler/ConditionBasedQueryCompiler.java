//package org.litebridge.orm.engine.compiler;
//
//import org.jspecify.annotations.Nullable;
//import org.litebridge.db.spi.Column;
//import org.litebridge.db.spi.Table;
//import org.litebridge.orm.api.select.SelectTerminal;
//import org.litebridge.orm.api.select.model.SelectSpec;
//import org.litebridge.orm.engine.LitebridgeContext;
//import org.litebridge.orm.expression.ColumnExpressionSpec;
//import org.litebridge.orm.persistence.OrmTable;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Objects;
//
//
//abstract class ConditionBasedQueryCompiler<CC extends CompilationContext> extends AbstractQueryCompiler<CC>  {
//
//    private final Map<Class<?>, List<Table>> aliasHistory = new HashMap<>();
//    private final Map<Table, OrmTable> tableToOrmTableMap = new HashMap<>();
//
//    protected ConditionBasedQueryCompiler(final LitebridgeContext litebridgeContext) {
//        super(litebridgeContext);
//    }
//
//    protected final @Nullable Object resolveAliases(final @Nullable Object value, final @Nullable Table sourceAlias, final @Nullable Table targetAlias, boolean preferSource) {
//        switch (value) {
//            case null -> {
//                return null;
//            }
//            case Column column -> {
//                OrmTable ormTable = litebridgeContext.tableRegistry().getOrmTable(column.table());
//
//                if (ormTable == null && sourceAlias != null) {
//                    final OrmTable sourceOrmTable = tableToOrmTableMap.get(sourceAlias);
//                    if (sourceOrmTable != null) {
//                        ormTable = sourceOrmTable.getContextTableRegistry().getOrmTable(column.table());
//                    }
//                }
//
//                if (ormTable == null && targetAlias != null) {
//                    final OrmTable targetOrmTable = tableToOrmTableMap.get(targetAlias);
//                    if (targetOrmTable != null) {
//                        ormTable = targetOrmTable.getContextTableRegistry().getOrmTable(column.table());
//                    }
//                }
//
//                if (ormTable != null) {
//                    Table resolvedTable = null;
//
//                    // If we have explicit source/target aliases for this join context, use them
//                    if (sourceAlias != null && ormTable.dtoClass().equals(getTableDtoClass(sourceAlias))) {
//                        if (targetAlias != null && ormTable.dtoClass().equals(getTableDtoClass(targetAlias))) {
//                            // Ambiguous (self-join); use preference
//                            resolvedTable = preferSource ? sourceAlias : targetAlias;
//                        } else {
//                            resolvedTable = sourceAlias;
//                        }
//                    } else if (targetAlias != null && ormTable.dtoClass().equals(getTableDtoClass(targetAlias))) {
//                        resolvedTable = targetAlias;
//                    }
//
//                    if (resolvedTable == null) {
//                        // Fallback to most recent alias in history
//                        final List<Table> history = aliasHistory.get(ormTable.dtoClass());
//                        if (history != null && !history.isEmpty()) {
//                            resolvedTable = history.get(history.size() - 1);
//                        }
//                    }
//
//                    if (resolvedTable != null) {
//                        return new Column(resolvedTable, column.name(), column.alias());
//                    }
//                }
//            }
//            case ColumnExpressionSpec ces -> {
//                ces.setColumn((Column) Objects.requireNonNull(resolveAliases(ces.getColumn(), sourceAlias, targetAlias, preferSource)));
//                return ces;
//            }
//            default -> {
//            }
//        }
//
//        return value;
//    }
//
//    @Deprecated(forRemoval = true)
//    protected final SelectSpec createSelectSpec(final SelectTerminal<?> selectTerminal) {
////        final AbstractSelector<?, ?> selector = switch (selectTerminal) {
////            case DelegatingSelectTerminal<?, ?> delegating -> DelegatingSelectorInspector.getDelegate(delegating);
////            case AbstractSelector<?, ?> s -> s;
////            default ->
////                    throw new IllegalArgumentException("Unsupported terminal type: " + selectTerminal.getClass().getName());
////        };
////
////        return selector.compile();
//        throw new UnsupportedOperationException("Deprecated");
//    }
//
//    @SuppressWarnings("ConstantConditions")
//    private Class<?> getTableDtoClass(Table table) {
//        final OrmTable ormTable = tableToOrmTableMap.getOrDefault(table, litebridgeContext.tableRegistry().getOrmTable(table));
//        return ormTable != null ? ormTable.dtoClass() : null;
//    }
//}
