package org.litebridge.orm.e2e.setup;

import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class MultiDbTestExtension implements TestTemplateInvocationContextProvider {
    @Override
    public boolean supportsTestTemplate(ExtensionContext context) {
        return true;
    }

    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
        String env = System.getProperty("db.env", "all");

        if (env.equals("all")) {
            return Stream.of(
                    invocationContext(new H2DbEnvironment()),
                    invocationContext(new OracleDbEnvironment())
            );
        } else if (env.equals("h2")) {
            return Stream.of(invocationContext(new H2DbEnvironment()));
        } else if (env.equals("oracle")) {
            return Stream.of(invocationContext(new OracleDbEnvironment()));
        } else {
            throw new IllegalArgumentException("Invalid db.env value: " + env);
        }
    }

    private TestTemplateInvocationContext invocationContext(DbEnvironment env) {
        return new TestTemplateInvocationContext() {
            @Override
            public String getDisplayName(int invocationIndex) {
                return env.getName();
            }

            @Override
            public List<Extension> getAdditionalExtensions() {
                return Collections.singletonList(new ParameterResolver() {
                    @Override
                    public boolean supportsParameter(ParameterContext pc, ExtensionContext ec) {
                        return pc.getParameter().getType().equals(DbEnvironment.class);
                    }

                    @Override
                    public Object resolveParameter(ParameterContext pc, ExtensionContext ec) {
                        return env;
                    }
                });
            }
        };
    }
}