package org.litebridgedb.orm.e2e.lob.entity;

import org.litebridgedb.orm.annotation.AllowInterface;
import org.litebridgedb.orm.annotation.Column;
import org.litebridgedb.orm.annotation.Table;

@Table("lb.blob_test")
@AllowInterface(BlobTestEntity.class)
public class PostgresBlobTestEntity extends BlobTestEntity {

    @Column("id")
    @Override
    public Long getId() {
        return super.getId();
    }

    @Column("blob_data")
    @Override
    public byte[] getData() {
        return super.getData();
    }
}
