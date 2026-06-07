package org.litebridgedb.orm.e2e.lob.entity;

public abstract class BlobTestEntity {

    private Long id;
    private byte[] data;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(final byte[] data) {
        this.data = data;
    }
}
