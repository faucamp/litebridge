package org.litebridge.spring.boot.autoconfigure;

import org.litebridge.orm.LitebridgeCore;
import org.litebridge.spring.repository.LitebridgeRepository;
import org.litebridge.spring.repository.LitebridgeRepositoryConfigurationExtension;
import org.litebridge.spring.repository.LitebridgeRepositoryFactoryBean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Import;

/**
 * Auto-configuration for Litebridge repositories.
 */
@AutoConfiguration(after = LitebridgeAutoConfiguration.class)
@ConditionalOnBean(LitebridgeCore.class)
@ConditionalOnClass(LitebridgeRepository.class)
@ConditionalOnMissingBean({LitebridgeRepositoryFactoryBean.class, LitebridgeRepositoryConfigurationExtension.class})
@ConditionalOnProperty(prefix = "spring.data.litebridge.repositories", name = "enabled", havingValue = "true", matchIfMissing = true)
@Import(LitebridgeRepositoriesAutoConfigureRegistrar.class)
public class LitebridgeRepositoriesAutoConfiguration {

}
