package org.litebridgedb.maven.config.reverse;

import org.apache.maven.plugins.annotations.Parameter;

import java.util.List;
import java.util.StringJoiner;

/**
 * Input configuration for reverse engineering.
 */
public final class RevEngInputConfig {

    /**
     * List of tables names to generate entities for.
     * <p>
     * The table names should be qualified with the schema name if applicable.
     * In addition to specifying tables to map, this allows customisation of the table mapping.
     */
    @Parameter(required = true)
    private List<String> tables;

    public List<String> getTables() {
        return tables;
    }

    public void setTables(final List<String> tables) {
        this.tables = tables;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", RevEngInputConfig.class.getSimpleName() + "[", "]")
                .add("tables=" + tables)
                .toString();
    }
}
