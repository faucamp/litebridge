package org.litebridge.db.spi;

/**
 * Information about the capabilities of the database provider.
 * <p>
 * This influences how the ORM interacts with the database provider when compiling statements.
 *
 * @param supportsSequenceColumnValueGenerator Whether the database provider supports sequence-based column value generation.
 * @param insertCapability                     The capability of the database provider for handling INSERT operations.
 */
public record DatabaseProviderMetaData(
        boolean supportsSequenceColumnValueGenerator,
        InsertCapability insertCapability) {

    public enum InsertCapability {
        NATIVE_MULTIROW,
        BATCHED_INSERTS,
        SINGLE_INSERTS
    }
}
