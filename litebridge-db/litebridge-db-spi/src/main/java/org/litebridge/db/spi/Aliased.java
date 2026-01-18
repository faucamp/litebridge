package org.litebridge.db.spi;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * A name and an optional alias.
 * <p>
 * This class provides functionality to create and manage aliased entities.
 * It is designed to be extended by other classes that require naming and optional aliasing.
 */
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

    /**
     * Retrieve the name of the aliased entity.
     *
     * @return the name of the aliased entity
     */
    public String name() {
        return name;
    }

    /**
     * Retrieve the alias of the aliased entity.
     *
     * @return the alias of the aliased entity, or {@code null} if no alias is set
     */
    public @Nullable String alias() {
        return alias;
    }

    /**
     * Retrieve the alias if it is set; otherwise, return the name.
     *
     * @return the alias of the entity if it exists, or the name if no alias is set
     */
    public String aliasOrName() {
        return alias != null ? alias : name;
    }

    /**
     * Set the alias for this entity and return the updated instance.
     *
     * @param alias the alias to assign to this entity; must not be null
     * @return the updated instance of {@code Aliased} with the specified alias set
     */
    public Aliased as(final String alias) {
        setAlias(alias);
        return this;
    }

    /**
     * Create a new instance of {@code Aliased} with the specified name.
     * <p>
     * This is shorthand for {@code new Aliased(name)}.
     *
     * @param name the target name for the aliased entity, must not be null
     * @return a new {@code Aliased} instance initialized with the given name
     */
    public static Aliased a(final String name) {
        return new Aliased(name);
    }

    /**
     * Set the alias for this entity.
     *
     * @param alias the alias to assign to this entity; must not be null
     */
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
