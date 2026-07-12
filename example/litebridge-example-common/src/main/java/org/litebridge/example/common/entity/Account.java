package org.litebridge.example.common.entity;

import org.litebridge.orm.annotation.Column;
import org.litebridge.orm.annotation.Table;

import java.math.BigInteger;
import java.util.Objects;
import java.util.StringJoiner;

@Table("LB.ACCOUNT")
public class Account {

    @Column(value = "ACCOUNT_ID", generateUsingSequence = "LB.ACCOUNT_SEQ")
    private Long id;
    @Column("ACCOUNT_NAME")
    private String name;
    @Column("BALANCE")
    private BigInteger balance;
    @Column(value = "PERSON_ID", joinUsing = true)
    private Person owner;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigInteger getBalance() {
        return balance;
    }

    public void setBalance(final BigInteger balance) {
        this.balance = balance;
    }

    public Person getOwner() {
        return owner;
    }

    public void setOwner(Person owner) {
        this.owner = owner;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof final Account account)) return false;
        return Objects.equals(id, account.id) && Objects.equals(name, account.name) && Objects.equals(balance, account.balance) && Objects.equals(owner, account.owner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, balance, owner);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Account.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("name='" + name + "'")
                .add("balance=" + balance)
                .add("owner=" + owner)
                .toString();
    }
}
