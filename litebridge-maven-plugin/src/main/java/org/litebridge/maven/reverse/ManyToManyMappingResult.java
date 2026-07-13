package org.litebridge.maven.reverse;

import java.util.List;
import java.util.Set;

public record ManyToManyMappingResult(List<ManyToManyMapping> mappings, Set<String> collapsedTables) {
}
