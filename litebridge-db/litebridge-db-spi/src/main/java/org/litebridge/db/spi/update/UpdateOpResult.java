package org.litebridge.db.spi.update;

/**
 * Marker interface for the result of an update operation performed on the database.
 */
public sealed interface UpdateOpResult extends Result permits UpdateResult, BatchUpdateResult {
}
