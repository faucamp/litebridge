package org.litebridge.orm.api.dto.update;

import org.litebridge.orm.api.update.UpdateSetStep;
import org.litebridge.orm.expression.ColumnExpressionSpec;
import org.litebridge.orm.meta.QueryField;

/**
 * The starting interface for DTO update queries.
 *
 * @param <DTO> the DTO type
 */
public interface DtoUpdateStart<DTO> {

    /**
     * Sets a field by name.
     *
     * @param field the field name
     * @return the update set step
     */
    UpdateSetStep<DtoUpdateStep<DTO>> set(final String field);

    /**
     * Sets a field by expression.
     *
     * @param field the column expression
     * @return the update set step
     */
    UpdateSetStep<DtoUpdateStep<DTO>> set(final ColumnExpressionSpec field);

    /**
     * Sets a field by metamodel field.
     *
     * @param field the query field
     * @return the update set step
     */
    UpdateSetStep<DtoUpdateStep<DTO>> set(final QueryField field);

}
