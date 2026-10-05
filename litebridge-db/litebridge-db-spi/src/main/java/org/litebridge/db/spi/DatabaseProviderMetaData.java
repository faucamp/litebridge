package org.litebridge.db.spi;

/**
 * Information about the capabilities of the database provider.
 * <p>
 * This influences how the ORM interacts with the database provider when compiling statements.
 *
 * @param mergeCapability                      Level of support for {@code MERGE INTO} operations provided by the datbase.
 * @param supportsSequenceColumnValueGenerator Whether the database provider supports sequence-based column value generation.
 * @param insertCapability                     The capability of the database provider for handling {@code INSERT} operations.
 */
public record DatabaseProviderMetaData(
        boolean supportsSequenceColumnValueGenerator,
        MergeCapability mergeCapability,
        InsertCapability insertCapability) {

    /**
     * Insert operation capability.
     */
    public enum InsertCapability {
        /**
         * Natively supports {@code INSERT} statements with multiple rows.
         */
        NATIVE_MULTIROW,
        /**
         * Use JDBC batched insert statements for multi-row inserts.
         */
        BATCHED_INSERTS
    }

    /**
     * Merge operation capability.
     */
    public enum MergeCapability {
        /**
         * {@code MERGE} not supported.
         */
        NOT_SUPPORTED,
        /**
         * {@code MERGE} supports {@code USING (VALUES)}.
         */
        USING_VALUES,
        /**
         * {@code MERGE} does not support {@code USING (VALUES)}; use a subselect instead.
         */
        USING_VALUES_SUBQUERY
    }
}
