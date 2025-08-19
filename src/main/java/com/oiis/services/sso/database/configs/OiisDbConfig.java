package com.oiis.services.sso.database.configs;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.oiis.services.sso.database.repositories.oiis",
        entityManagerFactoryRef = "oiisEntityManagerFactory",
        transactionManagerRef = "oiisTransactionManager"
)
public class OiisDbConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.oiis")
    @Primary
    public DataSource oiisDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "oiisEntityManagerFactory")
    @Primary
    public LocalContainerEntityManagerFactoryBean oiisEntityManagerFactory(
            EntityManagerFactoryBuilder builder) {

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        properties.put("hibernate.globally_quoted_identifiers", true);

        return builder
                .dataSource(oiisDataSource())
                .packages("com.oiis.services.sso.database.entities.oiis")
                .persistenceUnit("oiis")
                .properties(properties)
                .build();
    }

    @Bean(name = "oiisTransactionManager")
    @Primary
    public PlatformTransactionManager oiisTransactionManager(
            @Qualifier("oiisEntityManagerFactory") EntityManagerFactory oiisEntityManagerFactory) {
        return new JpaTransactionManager(oiisEntityManagerFactory);
    }
}
