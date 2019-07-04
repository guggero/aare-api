package ch.aare.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories("ch.aare.api.control")
@EnableTransactionManagement
public class DatabaseConfiguration {
}
