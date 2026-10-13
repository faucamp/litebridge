package org.litebridge.commons;

import org.jspecify.annotations.Nullable;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;

/**
 * Utility class for Java Module System (JPMS) related operations.
 */
public class ModuleUtils {

    private ModuleUtils() {
    }

    /**
     * Ensures that the specified class is accessible to the current module.
     *
     * @param <T>      the type of the class
     * @param dtoClass the class to check for accessibility
     * @return the specified class if it is accessible
     * @throws IllegalArgumentException if the class is not accessible
     */
    public static <T> Class<T> requireAccessible(final Class<T> dtoClass) {
        return requireAccessible(dtoClass, MethodHandles.lookup(), ModuleUtils.class.getModule());
    }

    /**
     * Ensures that the specified class is accessible to the current module using the provided lookup.
     *
     * @param <T>      the type of the class
     * @param dtoClass the class to check for accessibility
     * @param lookup   the MethodHandles.Lookup instance to check accessibility with
     * @return the specified class if it is accessible
     * @throws IllegalArgumentException if the class is not accessible
     */
    public static <T> Class<T> requireAccessible(final Class<T> dtoClass, final @Nullable Lookup lookup) {
        return requireAccessible(dtoClass, lookup, ModuleUtils.class.getModule());
    }

    /**
     * Ensures that the specified class is accessible to the target module.
     *
     * @param <T>           the type of the class
     * @param dtoClass      the class to check for accessibility
     * @param callingModule the module that needs access to the class
     * @return the specified class if it is accessible
     * @throws IllegalArgumentException if the class is not accessible
     */
    public static <T> Class<T> requireAccessible(final Class<T> dtoClass, final Module callingModule) {
        return requireAccessible(dtoClass, MethodHandles.lookup(), callingModule);
    }

    /**
     * Ensures that the specified class is accessible to the target module using the provided lookup.
     *
     * @param <T>           the type of the class
     * @param dtoClass      the class to check for accessibility
     * @param lookup        the MethodHandles.Lookup instance to check accessibility with
     * @param callingModule the module that needs access to the class
     * @return the specified class if it is accessible
     * @throws IllegalArgumentException if the class is not accessible
     */
    public static <T> Class<T> requireAccessible(
            final Class<T> dtoClass,
            final @Nullable Lookup lookup,
            final Module callingModule) {
        final Module clientModule = dtoClass.getModule();

        // If it's the unnamed module (classpath), it's 'open' by default
        if (!clientModule.isNamed()) {
            return dtoClass;
        }

        // If it's a named module, check if it has 'opened' the package to us
        final String packageName = dtoClass.getPackageName();

        if (clientModule.isOpen(packageName, callingModule)) {
            return dtoClass;
        }

        if (lookup != null) {
            try {
                MethodHandles.privateLookupIn(dtoClass, lookup);
                return dtoClass;
            } catch (final IllegalAccessException | IllegalArgumentException ignored) {
                // Lookup does not grant access
            }
        }

        throw new IllegalArgumentException(String.format("Cannot access DTO class '%s'. Module '%s' does not open package '%s' to '%s'. " +
                        "Please ensure a valid MethodHandles.lookup() was passed during Litebridge initialisation, " +
                        "or add 'opens %s to %s;' to your module-info.java",
                dtoClass.getName(), clientModule.getName(), packageName, callingModule.getName(), packageName, callingModule.getName()));
    }
}
