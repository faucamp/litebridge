package org.litebridge.orm.persistence;

import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;

public class StatementChain {

    private final LinkedHashMap<Object, PipedStatement> dependencies = new LinkedHashMap<>();

    public LinkedHashMap<Object, PipedStatement> getDependencies() {
        return dependencies;
    }

    public @Nullable PipedStatement getDependency(final Object dto) {
        return dependencies.get(dto);
    }

    public void addDependency(final Object dto, final PipedStatement dependency) {
        dependencies.put(dto, dependency);
    }
}
