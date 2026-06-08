package org.litebridgedb.spring.boot.autoconfigure.test.entity;

import org.litebridgedb.orm.annotation.Column;
import org.litebridgedb.orm.annotation.Table;

@Table("LB.TEST_SCANNED_DTO")
public record TestScannedEntity(@Column("ID") Long id) {
}