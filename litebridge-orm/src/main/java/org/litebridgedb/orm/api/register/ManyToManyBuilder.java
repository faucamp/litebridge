package org.litebridgedb.orm.api.register;

/**
 * Builder class for defining many-to-many relationships between DTOs (Data Transfer Objects)
 * and their corresponding relationship tables in a database.
 * <p>
 * This class is part of the Litebridge ORM's fluent API for registering many-to-many mappings
 * between DTOs and their associated join tables. It provides an entry point for specifying the
 * details of the join table used in the many-to-many relationship.
 * <p>
 * Methods in this class return intermediate steps that allow further configuration of the
 * relationship, such as specifying the join columns and inverse join columns.
 * <p>
 * This builder is intended to simplify many-to-many table relationship configuration as part
 * of the table registration process within Litebridge ORM.
 */
public class ManyToManyBuilder {

    /**
     * Specifies the name of the join table to be used in a many-to-many relationship.
     * This method serves as the starting point for further configuration of the join
     * table, including definition of join columns and inverse join columns.
     *
     * @param table The name of the join table representing the many-to-many relationship
     *              in the underlying database. Must not be null or empty.
     * @return An instance of {@link ManyToManyBuilderJoinColumnStep} to allow further
     * configuration of the join columns for the specified join table.
     */
    public ManyToManyBuilderJoinColumnStep joinTable(final String table) {
        return new ManyToManyBuilderJoinColumnStep(table);
    }
}
