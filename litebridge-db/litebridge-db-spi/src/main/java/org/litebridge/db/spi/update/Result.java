package org.litebridge.db.spi.update;

/**
 * Marker interface for a result of an update operation performed on the database.
 */
public sealed interface Result permits InsertResult, UpdateOpResult {
}
