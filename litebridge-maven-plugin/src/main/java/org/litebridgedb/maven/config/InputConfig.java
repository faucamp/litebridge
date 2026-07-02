package org.litebridgedb.maven.config;

import java.util.List;
import java.util.StringJoiner;

public class InputConfig {

    /**
     * List of tables names to generate entities for.
     * <p>
     * The table names should be qualified with the schema name if applicable.
     * In addition to specifying tables to map, this allows customisation of the table mapping.
     */
    private List<String> tables;

    public List<String> getTables() {
        return tables;
    }

    public void setTables(final List<String> tables) {
        this.tables = tables;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", InputConfig.class.getSimpleName() + "[", "]")
                .add("tables=" + tables)
                .toString();
    }
}
