package org.litebridge.orm.e2e.lob.entity;

import org.litebridge.orm.annotation.AllowInterface;
import org.litebridge.orm.annotation.Column;
import org.litebridge.orm.annotation.Table;

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
