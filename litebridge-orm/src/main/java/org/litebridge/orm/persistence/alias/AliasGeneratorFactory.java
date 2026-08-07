package org.litebridge.orm.persistence.alias;

/**
 * Factory for creating {@link AliasGenerator} instances.
 */
public interface AliasGeneratorFactory {
    /**
     * Creates a new, fresh instance of an {@link AliasGenerator}.
     *
     * @return a new alias generator
     */
    AliasGenerator create();
}
