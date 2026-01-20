package org.litebridge.orm.e2e.dto;

import java.util.List;
import java.util.StringJoiner;

public class Address {

    private String street;
    private String city;
    private List<Person> tenants;

    public String getStreet() {
        return street;
    }

    public void setStreet(final String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(final String city) {
        this.city = city;
    }

    public List<Person> getTenants() {
        return tenants;
    }

    public void setTenants(final List<Person> tenants) {
        this.tenants = tenants;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Address.class.getSimpleName() + "[", "]")
                .add("street='" + street + "'")
                .add("city='" + city + "'")
                .add("tenants=" + tenants)
                .toString();
    }
}
