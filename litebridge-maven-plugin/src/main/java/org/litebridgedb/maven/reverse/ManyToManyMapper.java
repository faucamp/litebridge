package org.litebridgedb.maven.reverse;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.db.spi.ForeignKeyConstraint;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.TableMetaData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ManyToManyMapper {

    private ManyToManyMapper() {
    }

    public static List<ManyToManyMapping> extractManyToManyMappings(final Map<String, TableMetaData> tableMetaDataMap) {
        final Map<Table, List<JoinHalf>> referencedTables = new HashMap<>();
        final Set<Table> entityTables = tableMetaDataMap.values().stream()
                .map(TableMetaData::toTable)
                .collect(Collectors.toSet());

        for (final TableMetaData tableMetaData : tableMetaDataMap.values()) {
            for (final ColumnMetaData columnMetaData : tableMetaData.columns()) {

                for (ForeignKeyConstraint foreignKeyConstraint : columnMetaData.getForeignKeyConstraints()) {
                    final List<JoinHalf> joinHalves = referencedTables.computeIfAbsent(foreignKeyConstraint.foreignKey().table(), k -> new ArrayList<>());
                    joinHalves.add(new JoinHalf(tableMetaData, columnMetaData, foreignKeyConstraint.foreignKey()));
                }

                for (ForeignKeyConstraint foreignRef : columnMetaData.getForeignReferences()) {
                    final List<JoinHalf> joinHalves = referencedTables.computeIfAbsent(foreignRef.foreignKey().table(), k -> new ArrayList<>());
                    joinHalves.add(new JoinHalf(tableMetaData, columnMetaData, foreignRef.foreignKey()));
                }
            }
        }

        final List<ManyToManyMapping> mappings = new ArrayList<>();

        for (Map.Entry<Table, List<JoinHalf>> entry : referencedTables.entrySet()) {
            final Table joinTable = entry.getKey();
            final List<JoinHalf> referencingTableColumns = entry.getValue();

            // Check for join tables
            if (referencingTableColumns.size() != 2) {
                continue;
            }

            // See if the foreign key is referenced by a mapped table
            if (entityTables.contains(joinTable)) {
                // This table will be represented as an entity
                continue;
            }

            // The foreign key is referenced by two entities in the set; apply a many-to-many
            final TableMetaData leftTable = referencingTableColumns.getFirst().tableMetaData();
            final ColumnMetaData leftColumn = referencingTableColumns.getFirst().columnMetaData();
            final Column leftJoinColumn = referencingTableColumns.getFirst().joinColumn();
            final TableMetaData rightTable = referencingTableColumns.get(1).tableMetaData();
            final ColumnMetaData rightColumn = referencingTableColumns.get(1).columnMetaData();
            final Column rightJoinColumn = referencingTableColumns.get(1).joinColumn();
            mappings.add(new ManyToManyMapping(leftTable, leftColumn, joinTable, leftJoinColumn, rightJoinColumn, rightTable, rightColumn));
        }

        return mappings;
    }

    private record JoinHalf(TableMetaData tableMetaData, ColumnMetaData columnMetaData, Column joinColumn) {
    }
}
