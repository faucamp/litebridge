package org.litebridgedb.orm.e2e.lob.entity;

import org.litebridgedb.orm.annotation.AllowInterface;
import org.litebridgedb.orm.annotation.Column;
import org.litebridgedb.orm.annotation.Table;

@Table("BLOB_TEST")
@AllowInterface(BlobTestEntity.class)
public class SQLiteBlobTestEntity extends BlobTestEntity {

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
