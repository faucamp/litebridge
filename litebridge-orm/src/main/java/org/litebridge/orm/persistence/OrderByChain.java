package org.litebridge.orm.persistence;

public interface OrderByChain<T> {

    OrderByClosure<T> asc();

    OrderByClosure<T> desc();

}
