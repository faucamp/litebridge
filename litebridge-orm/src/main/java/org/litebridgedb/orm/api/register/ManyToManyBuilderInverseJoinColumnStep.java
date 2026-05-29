package org.litebridgedb.orm.api.register;

import org.litebridgedb.orm.api.spec.ManyToMany;

/**
 * Represents a step in the fluent API for defining the inverse join column in a many-to-many
 * relationship mapping. This class is part of the Litebridge ORM API for registering DTO-table
 * mappings and is used to specify the details of the inverse join column in a join table.
 * <p>
 * A many-to-many relationship involves three components:
 * - The join table that facilitates the relationship.
 * - The join column that references the primary key of the originating table.
 * - The inverse join column that references the primary key of the targeted table.
 * <p>
 * This step allows specifying the inverse join column and returns an instance
 * of {@link ManyToMany} that encapsulates the complete relationship details.
 * <p>
 * The {@code ManyToManyBuilderInverseJoinColumnStep} is a terminal step in the definition of a
 * many-to-many relationship and is created internally through methods in the parent builder class.
 */
public final class ManyToManyBuilderInverseJoinColumnStep {

    private final String joinTable;
    private final String joinColumn;

    ManyToManyBuilderInverseJoinColumnStep(final String joinTable, final String joinColumn) {
        this.joinTable = joinTable;
        this.joinColumn = joinColumn;
    }

    /**
     * Defines the inverse join column in the many-to-many relationship mapping.
     * An inverse join column specifies the column in the join table that references
     * the primary key of the target table in the relationship. This method finalizes
     * the configuration of the join table's details.
     *
     * @param column The name of the inverse join column in the join table. Must not
     *               be null or empty, as it represents a crucial reference for
     *               mapping the target entity.
     * @return A {@link ManyToMany} instance encapsulating the details of the
     * many-to-many relationship, including the join table, join column,
     * and inverse join column.
     */
    public ManyToMany inverseJoinColumn(final String column) {
        return new ManyToMany(joinTable, joinColumn, column);
    }
}