package org.litebridgedb.orm.e2e.manytomany.dto;

import org.litebridgedb.orm.e2e.basic.dto.Person;

import java.util.List;
import java.util.StringJoiner;

public class Group {

    private String name;
    private String description;
    private List<org.litebridgedb.orm.e2e.basic.dto.Person> members;

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
        return new StringJoiner(", ", Group.class.getSimpleName() + "[", "]")
                .add("name='" + name + "'")
                .add("description='" + description + "'")
                .add("members=" + members)
                .toString();
    }
}
