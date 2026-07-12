package org.litebridge.orm.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.tx.ConnectionProvider;
import org.litebridge.db.spi.update.Insert;
import org.litebridge.db.spi.update.InsertResult;
import org.litebridge.orm.Litebridge;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PersistenceFacadeCascadingTest {

    private DatabaseProvider databaseProvider;
    private Litebridge litebridge;
    private PersistenceFacade persistenceFacade;

    @BeforeEach
    void setUp() throws SQLException {
        databaseProvider = mock(DatabaseProvider.class);
        DataSource dataSource = mock(DataSource.class);
        litebridge = new Litebridge(databaseProvider, dataSource);

        // Use reflection to get persistenceFacade or just use litebridgedb.save()
        // Actually litebridgedb.save() calls persistenceFacade.save()
    }

    @Test
    void save_cascadingOneToOne() throws SQLException {
        // Given
        registerOneToOne();

        ParentDto parent = new ParentDto();
        parent.id = 1L;
        parent.name = "parent";

        ChildDto child = new ChildDto();
        child.id = 2L;
        child.name = "child";
        parent.child = child;

        when(databaseProvider.insert(any(Insert.class), any(ConnectionProvider.class)))
                .thenReturn(new InsertResult(1, Collections.emptyMap()));

        // When
        litebridge.save(parent);

        // Then
        verify(databaseProvider, times(2)).insert(any(Insert.class), any(ConnectionProvider.class));
    }

    private void registerOneToOne() throws SQLException {
        Table childTable = new Table("", "LB", "CHILD");
        ColumnMetaData childId = new ColumnMetaData(childTable, "ID", true, Types.BIGINT, 19);
        ColumnMetaData childName = new ColumnMetaData(childTable, "NAME", false, Types.VARCHAR, 255);
        doReturn(new TableMetaData(childTable, List.of("ID"), List.of(childId, childName)))
                .when(databaseProvider).tableMetaData(argThat(t -> t != null && "CHILD".equals(t.name())), any());

        Table parentTable = new Table("", "LB", "PARENT");
        ColumnMetaData parentId = new ColumnMetaData(parentTable, "ID", true, Types.BIGINT, 19);
        ColumnMetaData parentName = new ColumnMetaData(parentTable, "NAME", false, Types.VARCHAR, 255);
        ColumnMetaData parentChildId = new ColumnMetaData(parentTable, "CHILD_ID", false, Types.BIGINT, 19);
        doReturn(new TableMetaData(parentTable, List.of("ID"), List.of(parentId, parentName, parentChildId)))
                .when(databaseProvider).tableMetaData(argThat(t -> t != null && "PARENT".equals(t.name())), any());

        litebridge.register(ChildDto.class, rc -> rc.mapToTable("LB.CHILD")
                .with(spec -> spec.mapField("id").toColumn("ID"))
                .with(spec -> spec.mapField("name").toColumn("NAME")));

        litebridge.register(ParentDto.class, rc -> rc.mapToTable("LB.PARENT")
                .with(spec -> spec.mapField("id").toColumn("ID"))
                .with(spec -> spec.mapField("name").toColumn("NAME"))
                .with(spec -> spec.mapField("child").toColumn("CHILD_ID").joinOn("ID")));
    }

    @Test
    void save_cascadingOneToMany() throws SQLException {
        // Given
        registerOneToMany();

        ParentDto1Nm parent = new ParentDto1Nm();
        parent.id = 1L;
        parent.name = "parent";

        ChildDto1Nm child1 = new ChildDto1Nm();
        child1.id = 101L;
        child1.name = "child1";

        ChildDto1Nm child2 = new ChildDto1Nm();
        child2.id = 102L;
        child2.name = "child2";

        parent.children = new ArrayList<>(List.of(child1, child2));

        when(databaseProvider.insert(any(Insert.class), any(ConnectionProvider.class)))
                .thenReturn(new InsertResult(1, Collections.emptyMap()));

        // When
        litebridge.save(parent);

        // Then
        // 1 for parent, 2 for children
        verify(databaseProvider, times(3)).insert(any(Insert.class), any(ConnectionProvider.class));
    }

    private void registerOneToMany() throws SQLException {
        Table childTable = new Table("", "LB", "CHILD_1NM");
        ColumnMetaData childId = new ColumnMetaData(childTable, "ID", true, Types.BIGINT, 19);
        ColumnMetaData childName = new ColumnMetaData(childTable, "NAME", false, Types.VARCHAR, 255);
        ColumnMetaData parentId = new ColumnMetaData(childTable, "PARENT_ID", false, Types.BIGINT, 19);
        doReturn(new TableMetaData(childTable, List.of("ID"), List.of(childId, childName, parentId)))
                .when(databaseProvider).tableMetaData(argThat(t -> t != null && "CHILD_1NM".equals(t.name())), any());

        Table parentTable = new Table("", "LB", "PARENT_1NM");
        ColumnMetaData pId = new ColumnMetaData(parentTable, "ID", true, Types.BIGINT, 19);
        ColumnMetaData pName = new ColumnMetaData(parentTable, "NAME", false, Types.VARCHAR, 255);
        doReturn(new TableMetaData(parentTable, List.of("ID"), List.of(pId, pName)))
                .when(databaseProvider).tableMetaData(argThat(t -> t != null && "PARENT_1NM".equals(t.name())), any());

        litebridge.register(ChildDto1Nm.class, rc -> rc.mapToTable("LB.CHILD_1NM")
                .with(spec -> spec.mapField("id").toColumn("ID"))
                .with(spec -> spec.mapField("name").toColumn("NAME"))
                .with(spec -> spec.mapField("parentId").toColumn("PARENT_ID")));

        litebridge.register(ParentDto1Nm.class, rc -> rc.mapToTable("LB.PARENT_1NM")
                .with(spec -> spec.mapField("id").toColumn("ID"))
                .with(spec -> spec.mapField("name").toColumn("NAME"))
                .with(spec -> spec.mapField("children").oneToMany(b -> b.mappedByField("parentId"))));
    }

    public static class ParentDto1Nm {
        Long id;
        String name;
        List<ChildDto1Nm> children;
    }

    public static class ChildDto1Nm {
        Long id;
        String name;
        Long parentId;
    }

    public static class ParentDto {
        Long id;
        String name;
        ChildDto child;
    }

    public static class ChildDto {
        Long id;
        String name;
    }

    @Test
    void save_cascadingManyToMany() throws SQLException {
        // Given
        registerManyToMany();

        GroupDto group = new GroupDto();
        group.id = 1L;
        group.name = "group";

        UserDto user1 = new UserDto();
        user1.id = 10L;
        user1.name = "user1";

        group.users = new ArrayList<>(List.of(user1));

        when(databaseProvider.insert(any(Insert.class), any(ConnectionProvider.class)))
                .thenReturn(new InsertResult(1, Collections.emptyMap()));

        // When
        litebridge.save(group);

        // Then
        // 1 for group, 1 for user, 1 for junction table
        verify(databaseProvider, times(3)).insert(any(Insert.class), any(ConnectionProvider.class));
    }

    private void registerManyToMany() throws SQLException {
        Table userTable = new Table("", "LB", "USERS");
        ColumnMetaData userId = new ColumnMetaData(userTable, "ID", true, Types.BIGINT, 19);
        ColumnMetaData userName = new ColumnMetaData(userTable, "NAME", false, Types.VARCHAR, 255);
        doReturn(new TableMetaData(userTable, List.of("ID"), List.of(userId, userName)))
                .when(databaseProvider).tableMetaData(argThat(t -> t != null && "USERS".equals(t.name())), any());

        Table groupTable = new Table("", "LB", "GROUPS");
        ColumnMetaData groupId = new ColumnMetaData(groupTable, "ID", true, Types.BIGINT, 19);
        ColumnMetaData groupName = new ColumnMetaData(groupTable, "NAME", false, Types.VARCHAR, 255);
        doReturn(new TableMetaData(groupTable, List.of("ID"), List.of(groupId, groupName)))
                .when(databaseProvider).tableMetaData(argThat(t -> t != null && "GROUPS".equals(t.name())), any());

        Table junctionTable = new Table("", "LB", "GROUP_USER");
        ColumnMetaData jGroupId = new ColumnMetaData(junctionTable, "GROUP_ID", false, Types.BIGINT, 19);
        ColumnMetaData jUserId = new ColumnMetaData(junctionTable, "USER_ID", false, Types.BIGINT, 19);
        doReturn(new TableMetaData(junctionTable, List.of(), List.of(jGroupId, jUserId)))
                .when(databaseProvider).tableMetaData(argThat(t -> t != null && "GROUP_USER".equals(t.name())), any());

        litebridge.register(UserDto.class, rc -> rc.mapToTable("LB.USERS")
                .with(spec -> spec.mapField("id").toColumn("ID"))
                .with(spec -> spec.mapField("name").toColumn("NAME")));

        litebridge.register(GroupDto.class, rc -> rc.mapToTable("LB.GROUPS")
                .with(spec -> spec.mapField("id").toColumn("ID"))
                .with(spec -> spec.mapField("name").toColumn("NAME"))
                .with(spec -> spec.mapField("users").manyToMany(b -> b.joinTable("GROUP_USER").joinColumn("GROUP_ID").inverseJoinColumn("USER_ID"))));
    }

    public static class GroupDto {
        Long id;
        String name;
        List<UserDto> users;
    }

    public static class UserDto {
        Long id;
        String name;
    }
}
