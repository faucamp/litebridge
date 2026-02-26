package org.litebridge.orm.persistence;

import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;

public class StatementChain {

    private final LinkedHashMap<Object, PipedStatement> dependants = new LinkedHashMap<>();
    private final LinkedHashMap<Object, PipedStatement> dependencies = new LinkedHashMap<>();

    public LinkedHashMap<Object, PipedStatement> getDependencies() {
        return dependencies;
    }

    public LinkedHashMap<Object, PipedStatement> getDependants() {
        return dependants;
    }

    public @Nullable PipedStatement getDependency(final Object dto) {
        return dependencies.get(dto);
    }

    public void addDependency(final Object dto, final PipedStatement dependency) {
        dependencies.put(dto, dependency);
    }

    public void addDependant(final Object dto, final PipedStatement dependant) {
        dependants.put(dto, dependant);
    }
}
