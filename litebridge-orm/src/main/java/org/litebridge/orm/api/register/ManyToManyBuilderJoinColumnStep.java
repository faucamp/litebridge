package org.litebridge.orm.api.register;

/**
 * Represents a configuration step in the fluent API for defining many-to-many relationships
 * between DTOs (Data Transfer Objects) and their corresponding join tables in a database.
 * <p>
 * This class is specifically used to specify the join column for the join table in a many-to-many
 * relationship. It serves as an intermediate step in the configuration, allowing the user to
 * proceed to defining the inverse join column after setting the join column.
 * <p>
 * Instances of this class are created by the {@code joinOrmTable} method of {@link ManyToManyBuilder}
 * and provide a method for specifying the join column in the relationship.
 */
public class ManyToManyBuilderJoinColumnStep {

    private final String joinTable;

    ManyToManyBuilderJoinColumnStep(final String joinTable) {
        this.joinTable = joinTable;
    }

    /**
     * Defines the join column for a many-to-many relationship's join table.
     * This method is used to specify the column in the join table that corresponds
     * to the relationship between the two DTOs (Data Transfer Objects) in a many-to-many mapping.
     *
     * @param column The name of the join column in the join table. This column represents
     *               the foreign key for the primary DTO in the many-to-many relationship.
     *               Must not be null or empty.
     * @return An instance of {@link ManyToManyBuilderInverseJoinColumnStep} to allow further
     * configuration of the inverse join column in the many-to-many relationship.
     */
    public ManyToManyBuilderInverseJoinColumnStep joinColumn(final String column) {
        return new ManyToManyBuilderInverseJoinColumnStep(joinTable, column);
    }
}