package org.litebridge.db.spi.query;

public class SelectField {

    private final String name;
    private String alias;

    public SelectField(final String name) {
        this.name = name;
    }

    public SelectField(final String name, final String alias) {
        this.name = name;
        this.alias = alias;
    }

    public String name() {
        return name;
    }

    public String alias() {
        return alias;
    }

    SelectField as(String alias) {
        this.alias = alias;
        return this;
    }
}
