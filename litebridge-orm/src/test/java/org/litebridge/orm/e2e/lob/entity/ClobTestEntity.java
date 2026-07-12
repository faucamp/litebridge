package org.litebridge.orm.e2e.lob.entity;

public abstract class ClobTestEntity {

    private Long id;
    private String data;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getData() {
        return data;
    }

    public void setData(final String data) {
        this.data = data;
    }
}
