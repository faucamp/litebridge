package org.litebridge.orm.persistence;

import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;

/**
 * Manages dependencies and dependents between piped database statements for related entities.
 */
public class StatementChain {

    private final LinkedHashMap<Object, PipedStatement> dependants = new LinkedHashMap<>();
    private final LinkedHashMap<Object, PipedStatement> dependencies = new LinkedHashMap<>();

    /**
     * Returns the map of dependencies keyed by DTO instance.
     *
     * @return the map of dependencies
     */
    public LinkedHashMap<Object, PipedStatement> getDependencies() {
        return dependencies;
    }

    /**
     * Returns the map of dependents keyed by DTO instance.
     *
     * @return the map of dependents
     */
    public LinkedHashMap<Object, PipedStatement> getDependants() {
        return dependants;
    }

    /**
     * Retrieves the piped statement dependency for the specified DTO, or {@code null} if none.
     *
     * @param dto the DTO instance
     * @return the corresponding {@link PipedStatement}, or {@code null} if not found
     */
    public @Nullable PipedStatement getDependency(final Object dto) {
        return dependencies.get(dto);
    }

    /**
     * Adds a statement dependency for the specified DTO instance.
     *
     * @param dto        the DTO instance
     * @param dependency the piped statement dependency
     */
    public void addDependency(final Object dto, final PipedStatement dependency) {
        dependencies.put(dto, dependency);
    }

    /**
     * Adds a dependent statement for the specified DTO instance.
     *
     * @param dto       the DTO instance
     * @param dependant the dependent piped statement
     */
    public void addDependant(final Object dto, final PipedStatement dependant) {
        dependants.put(dto, dependant);
    }
}
