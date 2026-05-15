package org.litebridgedb.orm.e2e.manytomany.dto;

import org.litebridgedb.orm.e2e.basic.dto.Account;
import org.litebridgedb.orm.e2e.basic.dto.Person;

import java.util.List;
import java.util.StringJoiner;

public class GroupedPerson extends Person {

    private List<Group> groups;

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(final List<Group> groups) {
        this.groups = groups;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Person.class.getSimpleName() + "[", "]")
                .add("id=" + getId())
                .add("name='" + getName() + "'")
                .add("surname='" + getSurname() + "'")
                .add("age=" + getAge())
                .add("eyeColour='" + getEyeColour() + "'")
                .add("accounts=" + (getAccounts() != null ? getAccounts().stream().map(Account::getId).toList().toString() : null))
                .add("groups=" + (groups != null ? groups.stream().map(Group::getName).toList().toString() : null))
                .toString();
    }
}
