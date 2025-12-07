package org.litebridge.db.api;


public record Column(String name, boolean nullable, int dataType, int size) {
}
