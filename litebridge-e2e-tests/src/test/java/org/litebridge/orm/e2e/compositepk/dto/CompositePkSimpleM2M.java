package org.litebridge.orm.e2e.compositepk.dto;

import java.util.List;

public final class CompositePkSimpleM2M {

    private Long pk1;
    private Long pk2;
    private String description;
    private List<CompositePkSimpleM2M> others;

    public Long getPk1() {
        return pk1;
    }

    public void setPk1(final Long pk1) {
        this.pk1 = pk1;
    }

    public Long getPk2() {
        return pk2;
    }

    public void setPk2(final Long pk2) {
        this.pk2 = pk2;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public List<CompositePkSimpleM2M> getOthers() {
        return others;
    }

    public void setOthers(final List<CompositePkSimpleM2M> others) {
        this.others = others;
    }
}
