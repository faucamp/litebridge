package org.litebridgedb.orm.e2e.setup;

import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
import org.litebridgedb.commons.StringUtils;

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
        final String[] envs = StringUtils.splitArray(System.getProperty("lb.e2e.env", "all"), ',', 0, false);

        if (envs.length == 1) {
            if (envs[0].equals("all")) {
                return Stream.of(
                        invocationContext(new H2DbEnvironment()),
                        invocationContext(new OracleDbEnvironment()),
                        invocationContext(new PostgresDbEnvironment()),
                        invocationContext(new SQLiteDbEnvironment()));
            } else if (envs[0].equals("none")) {
                return Stream.empty();
            }
        }

        final TestTemplateInvocationContext[] tests = new TestTemplateInvocationContext[envs.length];

        for (int i = 0; i < envs.length; i++) {
            final String env = envs[i];

            switch (env) {
                case "h2":
                    tests[i] = invocationContext(new H2DbEnvironment());
                    break;
                case "oracle":
                    tests[i] = invocationContext(new OracleDbEnvironment());
                    break;
                case "postgres":
                    tests[i] = invocationContext(new PostgresDbEnvironment());
                    break;
                case "sqlite":
                    tests[i] = invocationContext(new SQLiteDbEnvironment());
                    break;
                default:
                    throw new IllegalArgumentException("Invalid lb.e2e.env rhs: " + env);
            }
        }

        return Stream.of(tests);
    }

    @Override
    public boolean mayReturnZeroTestTemplateInvocationContexts(final ExtensionContext context) {
        return true;
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
                        return pc.getParameter().getType().equals(DbEnvironment.class)
                                || pc.getParameter().getType().equals(DbEnvDtoTableMapper.class);
                    }

                    @Override
                    public Object resolveParameter(ParameterContext pc, ExtensionContext ec) {
                        if (pc.getParameter().getType().equals(DbEnvironment.class)) {
                            return env;
                        } else if (pc.getParameter().getType().equals(DbEnvDtoTableMapper.class)) {
                            return env.getDtoTableMapper();
                        } else {
                            throw new IllegalArgumentException("Invalid parameter type: " + pc.getParameter().getType());
                        }
                    }
                });
            }
        };
    }
}