//package org.litebridge.db.spi.impl.function;
//
//import org.jspecify.annotations.Nullable;
//import org.litebridge.commons.StringUtils;
//import org.litebridge.db.spi.Column;
//import org.litebridge.db.spi.Operation;
//import org.litebridge.db.spi.Table;
//import org.litebridge.db.spi.expression.ClauseType;
//import org.litebridge.db.spi.expression.ColumnReference;
//import org.litebridge.db.spi.expression.DelegateExpression;
//import org.litebridge.db.spi.impl.sql.LabelGenerator;
//import org.litebridge.db.spi.query.Select;
//
//import java.util.StringJoiner;
//
//import static org.litebridge.db.spi.impl.function.DatabaseProviderContextUtil.getLabelGenerator;
//
///**
// * Implementation of {@link ColumnReference}.
// */
//public class ColumnReferenceImpl extends AliasReferenceImpl implements ColumnReference {
//
//    private final Column column;
//
//    /**
//     * Creates a new {@code ColumnReferenceImpl}.
//     *
//     * @param column The target selected column to reference.
//     * @param alias  Alias of the column.
//     */
//    public ColumnReferenceImpl(final Column column, final @Nullable String alias, final @Nullable String tableAlias) {
//        super(alias, tableAlias);
//        this.column = column;
//    }
//
//    @Override
//    public Column column() {
//        return column;
//    }
//
//    @Override
//    public String toSql(final Operation operation, final ClauseType clause, final @Nullable DelegateExpression parent, final Object providerContext) {
//        final LabelGenerator labelGenerator = getLabelGenerator(providerContext);
//
//        if (alias != null && clause != ClauseType.SELECT && clause != ClauseType.WHERE) {
//            return labelGenerator.quoteAlias(alias);
//        }
//
//        if (tableAlias != null) {
//            return labelGenerator.quoteAlias(tableAlias) + "." + labelGenerator.quoteIdentifier(column.name());
//        }
//
//        if (operation instanceof Select select
//                && select.expressions().isEmpty()) {
//            // Omit the table name since "*" was selected
//            return labelGenerator.quoteIdentifier(column.name());
//        }
//
//        final Table table = column.table();
//
//        if (table.isVirtual() && StringUtils.isBlank(table.name())) {
//            return labelGenerator.quoteIdentifier(column.name());
//        } else {
//            return labelGenerator.quoteIdentifier(table.name()) + "." + labelGenerator.quoteIdentifier(column.name());
//        }
//    }
//
//    @Override
//    public String toString() {
//        return new StringJoiner(", ", ColumnReferenceImpl.class.getSimpleName() + "[", "]")
//                .add("column=" + column)
//                .toString();
//    }
//}
