package org.litebridgedb.commons;

public class ModuleUtils {

    private ModuleUtils() {
    }

    public static <T> Class<T> requireAccessible(final Class<T> dtoClass) {
        final Module clientModule = dtoClass.getModule();

        // If it's the unnamed module (classpath), it's 'open' by default
        if (!clientModule.isNamed()) {
            return dtoClass;
        }

        // If it's a named module, check if it has 'opened' the package to us
        final Module litebridgeModule = ModuleUtils.class.getModule();
        final String packageName = dtoClass.getPackageName();

        if (!clientModule.isOpen(packageName, litebridgeModule)) {
            throw new IllegalArgumentException(String.format("Module '%s' does not open package '%s' to Litebridge. " +
                            "Please use the register(Lookup, Class, TableSpec) method " +
                            "or add 'opens %s to %s;' to your module-info.java",
                    clientModule.getName(), packageName, packageName, litebridgeModule.getName()));
        }

        return dtoClass;
    }
}
