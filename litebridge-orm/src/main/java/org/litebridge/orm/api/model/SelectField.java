package org.litebridge.orm.api.model;

public class SelectField {

    private final String name;
    private String alias;

    public SelectField(final String name) {
        this.name = name;
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
