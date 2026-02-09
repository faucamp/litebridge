package org.litebridge.orm.e2e.shareddto.dto;

public class Application {

    private String name;
    private Status status;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(final Status status) {
        this.status = status;
    }
}
