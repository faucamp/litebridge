package org.litebridge.orm.e2e.basic.dto;

import java.math.BigInteger;
import java.util.Objects;

public class PersonAccount {

    private Long id;
    private String name;
    private String surname;
    private int age;
    private Long accountId;
    private BigInteger accountBalance;
    private String accountName;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(final String surname) {
        this.surname = surname;
    }

    public int getAge() {
        return age;
    }

    public void setAge(final int age) {
        this.age = age;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(final Long accountId) {
        this.accountId = accountId;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(final String accountName) {
        this.accountName = accountName;
    }

    public BigInteger getAccountBalance() {
        return accountBalance;
    }

    public void setAccountBalance(final BigInteger accountBalance) {
        this.accountBalance = accountBalance;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof final PersonAccount that)) return false;
        return age == that.age && Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(surname, that.surname) && Objects.equals(accountId, that.accountId) && Objects.equals(accountName, that.accountName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, surname, age, accountId, accountName);
    }
}
