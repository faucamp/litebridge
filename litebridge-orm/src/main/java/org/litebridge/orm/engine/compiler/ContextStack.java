package org.litebridge.orm.engine.compiler;

import org.litebridge.db.spi.query.LogicOperator;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * An abstract stack used to manage a hierarchical context structure.
 * <p>
 * This class provides mechanisms to maintain a stack of context objects and
 * their associated hierarchical relationships.
 *
 * @param <T> The type of the context objects managed by this stack.
 */
public abstract class ContextStack<T> {

    private final List<T> all = new ArrayList<>();
    private final Deque<T> stack = new ArrayDeque<>();

    /**
     * Creates a new root instance for the context stack.
     *
     * @return The new root instance.
     */
    protected abstract T newRootInstance();

    /**
     * Creates a new child instance in the context stack.
     *
     * @return The new child instance.
     */
    protected abstract T newSubInstance(final LogicOperator logicOperator);

    /**
     * Retrieves the current context object from the stack.
     *
     * @return The current context object.
     */
    public T current() {
        if (stack.isEmpty()) {
            final T item = newRootInstance();
            all.add(item);
            stack.push(item);
            return item;
        }

        return stack.peek();
    }

    /**
     * Pushes a new child instance onto the context stack.
     *
     * @param logicOperator The logic operator to combine the new child instance with the current stack.
     * @return The new child instance.
     */
    public T push(final LogicOperator logicOperator) {
        return push(logicOperator, newSubInstance(logicOperator));
    }


    /**
     * Pushes a new child instance onto the context stack.
     *
     * @param logicOperator The logic operator to combine the new child instance with the current stack.
     * @param subgroup      The new child instance to push onto the stack.
     * @return The new child instance.
     */
    public T push(final LogicOperator logicOperator, final T subgroup) {
        all.add(subgroup);
        stack.push(subgroup);
        return subgroup;
    }

    /**
     * Removes the topmost child instance from the context stack.
     */
    public void pop() {
        stack.pop();
    }

    /**
     * Returns all stored instances in the context stack.
     *
     * @return A list of all stored instances.
     */
    public List<T> all() {
        return all;
    }

    /**
     * Checks if the context stack is empty.
     *
     * @return True if the stack is empty, false otherwise.
     */
    public boolean isEmpty() {
        return all.isEmpty();
    }

    /**
     * Clears all stored instances from the context stack.
     *
     * @since 0.5.1
     */
    public void clear() {
        stack.clear();
        all.clear();
    }
}
