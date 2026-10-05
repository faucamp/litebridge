package org.litebridge.orm.e2e.lob.entity;

import org.litebridge.orm.annotation.AllowInterface;
import org.litebridge.orm.annotation.Column;
import org.litebridge.orm.annotation.Table;

@Table("LB.BLOB_TEST")
@AllowInterface(BlobTestEntity.class)
public class DefaultBlobTestEntity extends BlobTestEntity {

    @Column("ID")
    @Override
    public Long getId() {
        return super.getId();
    }

    @Column("BLOB_DATA")
    @Override
    public byte[] getData() {
        return super.getData();
    }
}
