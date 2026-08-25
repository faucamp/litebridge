//package org.litebridge.orm.api.dto.update;
//
//import org.litebridge.orm.api.update.UpdateQuery;
//import org.litebridge.orm.api.update.UpdateStepBase;
//import org.litebridge.orm.expression.ExpressionSpec;
//
///**
// * The step interface for DTO update queries.
// *
// * @param <DTO> the DTO type
// */
//public sealed interface DtoUpdateStep<DTO>
//        extends DtoUpdateStart<DTO>, UpdateQuery
//        permits DtoUpdater {
//
//    /**
//     * Adds a where condition by field name.
//     *
//     * @param field the field name
//     * @return the where condition clause
//     */
//    DtoUpdateWhereConditionClause<DTO> where(final String field);
//
//    /**
//     * Adds a where condition by expression.
//     *
//     * @param expression the expression
//     * @return the where condition clause
//     */
//    DtoUpdateWhereConditionClause<DTO> where(final ExpressionSpec expression);
//}
