package org.litebridge.orm.e2e.manytomany.entity;

import org.litebridge.orm.annotation.Column;
import org.litebridge.orm.annotation.ManyToMany;
import org.litebridge.orm.annotation.Table;
import org.litebridge.orm.e2e.annotation.entity.Person;

import java.util.List;
import java.util.StringJoiner;

@Table("LB.GROUP")
public class GroupEntity {

    @Column("GROUP_NAME")
    private String name;
    @Column("GROUP_DESC")
    private String description;
    @ManyToMany(joinTable = "LB.PERSON_GROUP", joinColumn = "GROUP_NAME", inverseJoinColumn = "PERSON_ID")
    private List<Person> members;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public List<Person> getMembers() {
        return members;
    }

    public void setMembers(final List<Person> members) {
        this.members = members;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", GroupEntity.class.getSimpleName() + "[", "]")
                .add("name='" + name + "'")
                .add("description='" + description + "'")
                .add("members=" + members)
                .toString();
    }
}
