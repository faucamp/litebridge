package org.litebridge.spring.boot.autoconfigure;

import org.litebridge.spring.repository.LitebridgeRepositoriesRegistrar;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConditionalOnMissingBean(name = "litebridgeRepositoryConfigurationExtension")
@Import(LitebridgeRepositoriesRegistrar.class)
public class LitebridgeRepositoriesAutoConfiguration {
}
