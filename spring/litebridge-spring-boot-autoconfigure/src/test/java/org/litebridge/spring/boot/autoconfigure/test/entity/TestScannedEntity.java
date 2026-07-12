package org.litebridge.spring.boot.autoconfigure.test.entity;

import org.litebridge.orm.annotation.Column;
import org.litebridge.orm.annotation.Table;

@Table("LB.TEST_SCANNED_DTO")
public record TestScannedEntity(@Column("ID") Long id) {
}