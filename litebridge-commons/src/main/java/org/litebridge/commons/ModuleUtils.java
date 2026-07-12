package org.litebridge.commons;

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
        return requireAccessible(dtoClass, ModuleUtils.class.getModule());
    }

    /**
     * Ensures that the specified class is accessible to the target module.
     *
     * @param <T>          the type of the class
     * @param dtoClass     the class to check for accessibility
     * @param targetModule the module that needs access to the class
     * @return the specified class if it is accessible
     * @throws IllegalArgumentException if the class is not accessible
     */
    public static <T> Class<T> requireAccessible(final Class<T> dtoClass, final Module targetModule) {
        final Module clientModule = dtoClass.getModule();

        // If it's the unnamed module (classpath), it's 'open' by default
        if (!clientModule.isNamed()) {
            return dtoClass;
        }

        // If it's a named module, check if it has 'opened' the package to us
        final String packageName = dtoClass.getPackageName();

        if (!clientModule.isOpen(packageName, targetModule)) {
            throw new IllegalArgumentException(String.format("Module '%s' does not open package '%s' to '%s'. " +
                            "Please use the register(Lookup, Class, TableSpec) method " +
                            "or add 'opens %s to %s;' to your module-info.java",
                    clientModule.getName(), packageName, targetModule.getName(), packageName, targetModule.getName()));
        }

        return dtoClass;
    }
}
