package org.litebridge.spring.boot.autoconfigure;

import org.litebridge.db.spi.update.UpdateResult;
import org.litebridge.orm.Litebridge;
import org.litebridge.orm.LitebridgeCore;
import org.litebridge.orm.api.merge.DtoMergeUsingStep;
import org.litebridge.orm.api.merge.MergeTerminal;
import org.litebridge.orm.api.merge.SqlMergeUsingStep;

import java.util.function.Function;

/**
 * Wrapper class to expose Litebridge functionality throwing {@link UnsupportedOperationException}.
 * <p>
 * This makes dependency injection easier at the expense of having unsupported top-level API methods.
 */
public class LitebridgeCoreWrapper extends Litebridge {

    /**
     * Constructor.
     * <p>
     * Wraps a {@code LitebridgeCore} instance into the full {@code Litebridge} API.
     *
     * @param litebridgeCore {@code LitebridgeCore} instance to wrap.
     */
    public LitebridgeCoreWrapper(final LitebridgeCore litebridgeCore) {
        super(litebridgeCore);
    }

    @Override
    public void merge(final Object dto) {
        throw new UnsupportedOperationException("Merge not supported by Litebridge instance");
    }

    @Override
    public UpdateResult mergeInto(final String tableName, final Function<SqlMergeUsingStep, MergeTerminal> merge) {
        throw new UnsupportedOperationException("Merge not supported by Litebridge instance");
    }

    @Override
    public <DTO> UpdateResult mergeInto(final Class<DTO> dtoClass, final Function<DtoMergeUsingStep<DTO>, MergeTerminal> merge) {
        throw new UnsupportedOperationException("Merge not supported by Litebridge instance");
    }

    @Override
    public <DTO> UpdateResult mergeInto(final Class<DTO> dtoClass, final Class<?> contextDtoClass, final Function<DtoMergeUsingStep<DTO>, MergeTerminal> merge) {
        throw new UnsupportedOperationException("Merge not supported by Litebridge instance");
    }
}
