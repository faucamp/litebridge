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

    /**
     * Constructs a new {@code Aliased} instance with the specified name.
     *
     * @param name The target name.
     */
    public Aliased(final String name) {
        this.name = name;
    }

    /**
     * Constructs a new {@code Aliased} instance with the specified name and alias.
     *
     * @param name  The target name.
     * @param alias The target alias.
     */
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
     * Set the alias for this entity.
     *
     * @param alias the alias to assign to this entity; must not be null
     */
    public final void setAlias(final String alias) {
        this.alias = alias;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof final Aliased aliased)) return false;
        return equalsIgnoreAlias(aliased) && Objects.equals(alias, aliased.alias);
    }

    /**
     * Compares this instance with another {@code Aliased} instance, ignoring their aliases.
     *
     * @param aliased The other instance to compare with.
     * @return {@code true} if the names are equal, {@code false} otherwise.
     */
    public boolean equalsIgnoreAlias(final Aliased aliased) {
        return Objects.equals(name, aliased.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, alias);
    }
}
