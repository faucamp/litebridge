package org.litebridge.db.spi;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

public class Aliased {

    /**
     * Target name
     */
    private final String name;
    /**
     * Target alias
     */
    @Nullable
    private String alias;

    public Aliased(final String name) {
        this.name = name;
    }

    public Aliased(final String name, @Nullable final String alias) {
        this.name = name;
        this.alias = alias;
    }

    public String name() {
        return name;
    }

    public @Nullable String alias() {
        return alias;
    }

    public Aliased as(final String alias) {
        setAlias(alias);
        return this;
    }

    public static Aliased a(final String name) {
        return new Aliased(name);
    }

    final void setAlias(final String alias) {
        this.alias = alias;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof final Aliased aliased)) return false;
        return Objects.equals(name, aliased.name) && Objects.equals(alias, aliased.alias);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, alias);
    }
}
