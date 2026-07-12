package org.litebridge.orm.e2e.manytomany;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestTemplate;
import org.litebridge.orm.e2e.AbstractE2eTest;
import org.litebridge.orm.e2e.basic.dto.Person;
import org.litebridge.orm.e2e.manytomany.dto.Group;
import org.litebridge.orm.e2e.manytomany.dto.GroupedPerson;
import org.litebridge.orm.e2e.manytomany.entity.GroupEntity;
import org.litebridge.orm.e2e.manytomany.entity.GroupedPersonEntity;
import org.litebridge.orm.e2e.setup.DbEnvDtoTableMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class ManyToManyE2eTest extends AbstractE2eTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManyToManyE2eTest.class);

    @TestTemplate
    @DisplayName("Select DTO and join fetch related DTOs")
    void nestedDtos_fetchRelatedDtos(final DbEnvDtoTableMapper tableMapper) {
        registerDtoTableMappings(tableMapper);

        final GroupedPerson person1 = new GroupedPerson();
        person1.setName("Alice");
        person1.setSurname("Smith");
        person1.setAge(20);
        person1.setEyeColour("blue");

        final GroupedPerson person2 = new GroupedPerson();
        person2.setName("Bob");
        person2.setSurname("Jones");
        person2.setAge(22);
        person2.setEyeColour("brown");

        final GroupedPerson person3 = new GroupedPerson();
        person3.setName("Frank");
        person3.setSurname("Davies");
        person3.setAge(45);
        person3.setEyeColour("brown");

        final Group group1 = new Group();
        group1.setName("Group1");
        group1.setDescription("Test group 1");
        group1.setMembers(new ArrayList<>(List.of(person1, person2)));

        final Group group2 = new Group();
        group2.setName("Group2");
        group2.setDescription("Test group 2");
        group2.setMembers(List.of(person2, person3));

        // Save group 1
        LOGGER.info("Saving group 1");
        litebridge.save(group1);

        // Load group 1 as well as its members
        LOGGER.info("Loading group 1");
        final Group resultGroup1 = litebridge.select(Group.class)
                .join(GroupedPerson.class).on("members")
                .where("name").eq(group1.getName())
                .oneOrThrow();

        assertEquals(group1.getName(), resultGroup1.getName());
        assertNotNull(resultGroup1.getMembers());
        assertEquals(2, resultGroup1.getMembers().size());
        assertTrue(resultGroup1.getMembers().stream().anyMatch(p -> person1.getId().equals(p.getId())));
        assertTrue(resultGroup1.getMembers().stream().anyMatch(p -> person2.getId().equals(p.getId())));

        // Load member from group 1
        LOGGER.info("Loading member from group 1: person1");
        final GroupedPerson groupedMember1 = litebridge.select(GroupedPerson.class)
                .join(Group.class).on("groups")
                .where("id").eq(person1.getId())
                .oneOrThrow();

        assertEquals(person1.getName(), groupedMember1.getName());
        assertNotNull(groupedMember1.getGroups());
        assertEquals(1, groupedMember1.getGroups().size());
        assertEquals(group1.getName(), groupedMember1.getGroups().getFirst().getName());

        // Save group 2
        LOGGER.info("Saving group 2");
        litebridge.save(group2);

        // Load group 2 as well as its members
        LOGGER.info("Loading group 2");
        final Group resultGroup2 = litebridge.select(Group.class)
                .join(GroupedPerson.class).on("members")
                .where("name").eq(group2.getName())
                .oneOrThrow();

        assertEquals(group2.getName(), resultGroup2.getName());
        assertNotNull(resultGroup2.getMembers());
        assertEquals(2, resultGroup2.getMembers().size());
        assertTrue(resultGroup2.getMembers().stream().anyMatch(p -> person2.getId().equals(p.getId())));
        assertTrue(resultGroup2.getMembers().stream().anyMatch(p -> person3.getId().equals(p.getId())));

        // Load member from group 2
        LOGGER.info("Loading member from group 2: person2");
        final GroupedPerson groupedMember2 = litebridge.select(GroupedPerson.class)
                .join(Group.class).on("groups")
                .where("id").eq(person2.getId())
                .oneOrThrow();

        assertEquals(person2.getName(), groupedMember2.getName());
        assertNotNull(groupedMember2.getGroups());
        assertEquals(2, groupedMember2.getGroups().size());
        assertEquals(group2.getName(), groupedMember2.getGroups().getFirst().getName());
        assertEquals(group2.getName(), groupedMember2.getGroups().getFirst().getName());

        // Save group 2 again (should do nothing)
        LOGGER.info("Saving group 1 again (should do nothing)");
        litebridge.save(group1);

        // Add person3 to group1 and save
        LOGGER.info("Add person3 to group1 and save");
        group1.getMembers().add(person3);
        litebridge.save(group1);

        // Load group 1 again as well as its members
        LOGGER.info("Loading group 1 again");
        final Group resultGroup1Updated = litebridge.select(Group.class)
                .join(GroupedPerson.class).on("members")
                .where("name").eq(group1.getName())
                .oneOrThrow();

        assertEquals(group1.getName(), resultGroup1Updated.getName());
        assertNotNull(resultGroup1Updated.getMembers());
        assertEquals(3, resultGroup1Updated.getMembers().size());
        assertTrue(resultGroup1Updated.getMembers().stream().anyMatch(p -> person1.getId().equals(p.getId())));
        assertTrue(resultGroup1Updated.getMembers().stream().anyMatch(p -> person2.getId().equals(p.getId())));
        assertTrue(resultGroup1Updated.getMembers().stream().anyMatch(p -> person3.getId().equals(p.getId())));
    }

    @TestTemplate
    @DisplayName("Select DTO without related DTOs")
    void nestedDtos_dontfetchRelatedDtos(final DbEnvDtoTableMapper tableMapper) throws Exception {
        registerDtoTableMappings(tableMapper);

        final GroupedPerson person1 = new GroupedPerson();
        person1.setName("Alice");
        person1.setSurname("Smith");
        person1.setAge(20);
        person1.setEyeColour("blue");

        final GroupedPerson person2 = new GroupedPerson();
        person2.setName("Bob");
        person2.setSurname("Jones");
        person2.setAge(22);
        person2.setEyeColour("brown");

        final GroupedPerson person3 = new GroupedPerson();
        person3.setName("Frank");
        person3.setSurname("Davies");
        person3.setAge(45);
        person3.setEyeColour("brown");

        final Group group1 = new Group();
        group1.setName("Group1");
        group1.setDescription("Test group 1");
        group1.setMembers(new ArrayList<>(List.of(person1, person2)));

        final Group group2 = new Group();
        group2.setName("Group2");
        group2.setDescription("Test group 2");
        group2.setMembers(List.of(person2, person3));

        // Save group 1
        LOGGER.info("Saving group 1");
        litebridge.save(group1);

        // Load group 1
        LOGGER.info("Loading group 1");
        final Group resultGroup1 = litebridge.select(Group.class)
                .where("name").eq(group1.getName())
                .oneOrThrow();

        assertEquals(group1.getName(), resultGroup1.getName());
        assertTrue(resultGroup1.getMembers().isEmpty());

        // Load member from group 1
        LOGGER.info("Loading member from group 1: person1");
        final GroupedPerson groupedMember1 = litebridge.select(GroupedPerson.class)
                .where("id").eq(person1.getId())
                .oneOrThrow();

        assertEquals(person1.getName(), groupedMember1.getName());
        assertTrue(groupedMember1.getGroups().isEmpty());

        // Save group 2
        LOGGER.info("Saving group 2");
        litebridge.save(group2);

        // Load group 2
        LOGGER.info("Loading group 2");
        final Group resultGroup2 = litebridge.select(Group.class)
                .where("name").eq(group2.getName())
                .oneOrThrow();

        assertEquals(group2.getName(), resultGroup2.getName());
        assertTrue(resultGroup2.getMembers().isEmpty());

        // Load member from group 2
        LOGGER.info("Loading member from group 2: person2");
        final GroupedPerson groupedMember2 = litebridge.select(GroupedPerson.class)
                .where("id").eq(person2.getId())
                .oneOrThrow();

        assertEquals(person2.getName(), groupedMember2.getName());
        assertTrue(groupedMember2.getGroups().isEmpty());

        // Save group 2 again (should do nothing)
        LOGGER.info("Saving group 1 again (should do nothing)");
        litebridge.save(group1);

        // Add person3 to group1 and save
        LOGGER.info("Add person3 to group1 and save");
        group1.getMembers().add(person3);
        litebridge.save(group1);

        // Load group 1 again as well as its members
        LOGGER.info("Loading group 1 again, but with related members this time");
        final Group resultGroup1Updated = litebridge.select(Group.class)
                .join(GroupedPerson.class).on("members")
                .where("name").eq(group1.getName())
                .oneOrThrow();

        assertEquals(group1.getName(), resultGroup1Updated.getName());
        assertNotNull(resultGroup1Updated.getMembers());
        assertEquals(3, resultGroup1Updated.getMembers().size());
        assertTrue(resultGroup1Updated.getMembers().stream().anyMatch(p -> person1.getId().equals(p.getId())));
        assertTrue(resultGroup1Updated.getMembers().stream().anyMatch(p -> person2.getId().equals(p.getId())));
        assertTrue(resultGroup1Updated.getMembers().stream().anyMatch(p -> person3.getId().equals(p.getId())));
    }

    @TestTemplate
    @DisplayName("Annoted entities: Select entity and join fetch related entities")
    void nestedEntities_fetchRelatedEntities(final DbEnvDtoTableMapper tableMapper) {
        // Test entities specify the "LB" schema in the @Table annotation, so skip SQLite (no schema) and Postgres (lowercase)
        assumeTrue(!dbEnv.getName().equals("SQLite") && !dbEnv.getName().equals("PostgreSQL"));

        litebridge.register(GroupedPersonEntity.class);
        litebridge.register(GroupEntity.class);

        final GroupedPersonEntity person1 = new GroupedPersonEntity();
        person1.setName("Alice");
        person1.setSurname("Smith");
        person1.setAge(20);
        person1.setEyeColour("blue");

        final GroupedPersonEntity person2 = new GroupedPersonEntity();
        person2.setName("Bob");
        person2.setSurname("Jones");
        person2.setAge(22);
        person2.setEyeColour("brown");

        final GroupedPersonEntity person3 = new GroupedPersonEntity();
        person3.setName("Frank");
        person3.setSurname("Davies");
        person3.setAge(45);
        person3.setEyeColour("brown");

        final GroupEntity group1 = new GroupEntity();
        group1.setName("Group1");
        group1.setDescription("Test group 1");
        group1.setMembers(new ArrayList<>(List.of(person1, person2)));

        final GroupEntity group2 = new GroupEntity();
        group2.setName("Group2");
        group2.setDescription("Test group 2");
        group2.setMembers(List.of(person2, person3));

        // Save group 1
        LOGGER.info("Saving group 1");
        litebridge.save(group1);

        // Load group 1 as well as its members
        LOGGER.info("Loading group 1");
        final GroupEntity resultGroup1 = litebridge.select(GroupEntity.class)
                .join(GroupedPersonEntity.class).on("members")
                .where("name").eq(group1.getName())
                .oneOrThrow();

        assertEquals(group1.getName(), resultGroup1.getName());
        assertNotNull(resultGroup1.getMembers());
        assertEquals(2, resultGroup1.getMembers().size());
        assertTrue(resultGroup1.getMembers().stream().anyMatch(p -> person1.getId().equals(p.getId())));
        assertTrue(resultGroup1.getMembers().stream().anyMatch(p -> person2.getId().equals(p.getId())));

        // Load member from group 1
        LOGGER.info("Loading member from group 1: person1");
        final GroupedPersonEntity groupedMember1 = litebridge.select(GroupedPersonEntity.class)
                .join(GroupEntity.class).on("groups")
                .where("id").eq(person1.getId())
                .oneOrThrow();

        assertEquals(person1.getName(), groupedMember1.getName());
        assertNotNull(groupedMember1.getGroups());
        assertEquals(1, groupedMember1.getGroups().size());
        assertEquals(group1.getName(), groupedMember1.getGroups().getFirst().getName());

        // Save group 2
        LOGGER.info("Saving group 2");
        litebridge.save(group2);

        // Load group 2 as well as its members
        LOGGER.info("Loading group 2");
        final GroupEntity resultGroup2 = litebridge.select(GroupEntity.class)
                .join(GroupedPersonEntity.class).on("members")
                .where("name").eq(group2.getName())
                .oneOrThrow();

        assertEquals(group2.getName(), resultGroup2.getName());
        assertNotNull(resultGroup2.getMembers());
        assertEquals(2, resultGroup2.getMembers().size());
        assertTrue(resultGroup2.getMembers().stream().anyMatch(p -> person2.getId().equals(p.getId())));
        assertTrue(resultGroup2.getMembers().stream().anyMatch(p -> person3.getId().equals(p.getId())));

        // Load member from group 2
        LOGGER.info("Loading member from group 2: person2");
        final GroupedPersonEntity groupedMember2 = litebridge.select(GroupedPersonEntity.class)
                .join(GroupEntity.class).on("groups")
                .where("id").eq(person2.getId())
                .oneOrThrow();

        assertEquals(person2.getName(), groupedMember2.getName());
        assertNotNull(groupedMember2.getGroups());
        assertEquals(2, groupedMember2.getGroups().size());
        assertEquals(group2.getName(), groupedMember2.getGroups().getFirst().getName());
        assertEquals(group2.getName(), groupedMember2.getGroups().getFirst().getName());

        // Save group 2 again (should do nothing)
        LOGGER.info("Saving group 1 again (should do nothing)");
        litebridge.save(group1);

        // Add person3 to group1 and save
        LOGGER.info("Add person3 to group1 and save");
        group1.getMembers().add(person3);
        litebridge.save(group1);

        // Load group 1 again as well as its members
        LOGGER.info("Loading group 1 again");
        final GroupEntity resultGroup1Updated = litebridge.select(GroupEntity.class)
                .join(GroupedPersonEntity.class).on("members")
                .where("name").eq(group1.getName())
                .oneOrThrow();

        assertEquals(group1.getName(), resultGroup1Updated.getName());
        assertNotNull(resultGroup1Updated.getMembers());
        assertEquals(3, resultGroup1Updated.getMembers().size());
        assertTrue(resultGroup1Updated.getMembers().stream().anyMatch(p -> person1.getId().equals(p.getId())));
        assertTrue(resultGroup1Updated.getMembers().stream().anyMatch(p -> person2.getId().equals(p.getId())));
        assertTrue(resultGroup1Updated.getMembers().stream().anyMatch(p -> person3.getId().equals(p.getId())));
    }

    private void registerDtoTableMappings(final DbEnvDtoTableMapper tableMapper) {
        if (dbEnv.getName().equals("SQLite")) {
            litebridge.register(GroupedPerson.class, rc -> rc
                    .allowInterface(Person.class)
                    .mapToTable(tableMapper.qualifyName("PERSON"))
                    .with(spec -> spec.mapField("id").toColumn("PERSON_ID"))
                    .with(spec -> spec.mapField("name").toColumn("FIRST_NAME"))
                    .with(spec -> spec.mapField("groups").manyToMany(c -> c.joinTable(tableMapper.qualifyName("PERSON_GROUP"))
                            .joinColumn("PERSON_ID")
                            .inverseJoinColumn("GROUP_NAME"))));
        } else if (dbEnv.getName().equals("PostgreSQL")) {
            litebridge.register(GroupedPerson.class, rc -> rc
                    .allowInterface(Person.class)
                    .mapToTable("lb.person")
                    .with(spec -> spec.mapField("id").toColumn("person_id").generateUsingSequence("lb.person_seq"))
                    .with(spec -> spec.mapField("name").toColumn("first_name"))
                    .with(spec -> spec.mapField("groups").manyToMany(c -> c.joinTable("lb.person_group")
                            .joinColumn("person_id")
                            .inverseJoinColumn("group_name"))));
        } else {
            litebridge.register(GroupedPerson.class, rc -> rc
                    .allowInterface(Person.class)
                    .mapToTable(tableMapper.qualifyName("PERSON"))
                    .with(spec -> spec.mapField("id").toColumn("PERSON_ID").generateUsingSequence("LB.PERSON_SEQ"))
                    .with(spec -> spec.mapField("name").toColumn("FIRST_NAME"))
                    .with(spec -> spec.mapField("groups").manyToMany(c -> c.joinTable(tableMapper.qualifyName("PERSON_GROUP"))
                            .joinColumn("PERSON_ID")
                            .inverseJoinColumn("GROUP_NAME"))));
        }

        litebridge.register(Group.class, rc -> rc.mapToTable(tableMapper.qualifyName("GROUP"))
                .with(spec -> spec.mapField("name").toColumn(tableMapper.transformColumnName("GROUP_NAME")))
                .with(spec -> spec.mapField("description").toColumn(tableMapper.transformColumnName("GROUP_DESC")))
                .with(spec -> spec.mapField("members").manyToMany(c -> c.joinTable(tableMapper.qualifyName("PERSON_GROUP"))
                        .joinColumn(tableMapper.transformColumnName("GROUP_NAME"))
                        .inverseJoinColumn(tableMapper.transformColumnName("PERSON_ID")))));
    }
}