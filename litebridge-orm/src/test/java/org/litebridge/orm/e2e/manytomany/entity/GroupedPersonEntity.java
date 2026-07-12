package org.litebridge.orm.e2e.manytomany.entity;

import org.litebridge.orm.annotation.AllowInterface;
import org.litebridge.orm.annotation.ManyToMany;
import org.litebridge.orm.annotation.Table;
import org.litebridge.orm.e2e.annotation.entity.Account;
import org.litebridge.orm.e2e.annotation.entity.Person;

import java.util.List;
import java.util.StringJoiner;

@Table("LB.PERSON")
@AllowInterface(Person.class)
public class GroupedPersonEntity extends Person {

    @ManyToMany(joinTable = "LB.PERSON_GROUP", joinColumn = "PERSON_ID", inverseJoinColumn = "GROUP_NAME")
    private List<GroupEntity> groups;

    public List<GroupEntity> getGroups() {
        return groups;
    }

    public void setGroups(final List<GroupEntity> groups) {
        this.groups = groups;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", GroupedPersonEntity.class.getSimpleName() + "[", "]")
                .add("id=" + getId())
                .add("name='" + getName() + "'")
                .add("surname='" + getSurname() + "'")
                .add("age=" + getAge())
                .add("eyeColour='" + getEyeColour() + "'")
                .add("accounts=" + (getAccounts() != null ? getAccounts().stream().map(Account::getId).toList().toString() : null))
                .add("groups=" + (groups != null ? groups.stream().map(GroupEntity::getName).toList().toString() : null))
                .toString();
    }
}
