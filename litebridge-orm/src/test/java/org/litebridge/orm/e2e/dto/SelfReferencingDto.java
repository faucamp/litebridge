package org.litebridge.orm.e2e.dto;

public class SelfReferencingDto {

    private long id;
    private String myVar;
    private SelfReferencingDto parent;

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public String getMyVar() {
        return myVar;
    }

    public void setMyVar(final String myVar) {
        this.myVar = myVar;
    }

    public SelfReferencingDto getParent() {
        return parent;
    }

    public void setParent(final SelfReferencingDto parent) {
        this.parent = parent;
    }
}
