package org.litebridge.orm.engine;

import org.litebridge.db.spi.query.LogicOperator;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

abstract class ContextStack<T> {

    private final List<T> all = new ArrayList<>();
    private final Deque<T> stack = new ArrayDeque<>();

    protected abstract T newRootInstance();

    protected abstract T newSubInstance(final LogicOperator logicOperator);

    public T current() {
        if (stack.isEmpty()) {
            final T item = newRootInstance();
            all.add(item);
            stack.push(item);
            return item;
        }

        return stack.peek();
    }

    public T push(final LogicOperator logicOperator) {
        final T subgroup = newSubInstance(logicOperator);
        all.add(subgroup);
        stack.push(subgroup);
        return subgroup;
    }

    public void pop() {
        stack.pop();
    }

    public List<T> all() {
        return all;
    }

    public boolean isEmpty() {
        return all.isEmpty();
    }
}
