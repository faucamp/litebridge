package org.litebridgedb.orm.e2e.basic.dto;

import java.util.Objects;

public class Address {
    private Long id;
    private Person person;
    private String address;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(final Person person) {
        this.person = person;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(final String address) {
        this.address = address;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof final Address address1)) return false;
        return Objects.equals(id, address1.id) && Objects.equals(address, address1.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, address);
    }
}
