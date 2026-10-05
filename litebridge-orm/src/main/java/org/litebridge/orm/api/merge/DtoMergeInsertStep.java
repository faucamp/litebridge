package org.litebridge.orm.api.merge;

import org.litebridge.orm.api.insert.InsertValuesStep;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.InsertDtoValuesNode;
import org.litebridge.orm.engine.ast.InsertNode;

/**
 * DTO-mode merge step for setting up {@code WHEN NOT MATCHED INSERT} clause.
 */
public class DtoMergeInsertStep extends MergeInsertStep {

    /**
     * Creates a new {@code DtoMergeInsertStep} instance.
     *
     * @param table             name of the table to insert data into
     * @param litebridgeContext the Litebridge context
     */
    public DtoMergeInsertStep(final String table, final LitebridgeContext litebridgeContext) {
        super(table, litebridgeContext);
    }

    /**
     * Creates a {@code WHEN NOT MATCHED INSERT} clause inserting the specified DTO/entity.
     *
     * @param dto the DTO/entity to insert
     * @return the insert clause terminal
     */
    public MergeTerminal insert(final Object dto) {
        final InsertNode insertNode = new InsertNode(null, dto.getClass(), null, null, null);
        final InsertDtoValuesNode insertDtoValuesNode = new InsertDtoValuesNode(insertNode, dto);
        return new MergeTerminal(insertDtoValuesNode);
    }

    /**
     * Creates a {@code WHEN NOT MATCHED INSERT} clause inserting values into the specified DTO/entity fields.
     *
     * @param fields the DTO/entity fields to insert
     * @return step to specify the values to insert
     */
    @Override
    public InsertValuesStep insert(final String... fields) {
        return super.insert(fields);
    }
}
