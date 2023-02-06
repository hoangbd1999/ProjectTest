/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.vsat.collector.config.db;

import java.util.HashMap;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

/**
 *
 * @author ducnh
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.elcom",
        entityManagerFactoryRef = "vsatEntityManagerFactory",
        transactionManagerRef = "vsatTransactionManager")
public class PogresSqlConfiguration {

    @Autowired
    private Environment env;

    @Primary
    @Bean(name = "vsatDataSource")
    public DataSource vsatDataSource() {
        System.out.println("Loading config PostgreSQL Datasource...");
        final DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(env.getProperty("spring.datasource.postgres.driverClassName"));
        dataSource.setUrl(env.getProperty("spring.datasource.postgres.url"));
        dataSource.setUsername(env.getProperty("spring.datasource.postgres.username"));
        dataSource.setPassword(env.getProperty("spring.datasource.postgres.password"));
        return dataSource;
    }

    @Primary
    @Bean(name = "vsatEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean vsatEntityManagerFactory() {
        final LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(vsatDataSource());
        em.setPackagesToScan("com.elcom");

        final HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        final HashMap<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", env.getProperty("hibernate.hbm2ddl.auto"));
        properties.put("hibernate.dialect", env.getProperty("hibernate.dialect"));
        em.setJpaPropertyMap(properties);

        return em;
    }

    @Primary
    @Bean
    public PlatformTransactionManager vsatTransactionManager() {
        final JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(vsatEntityManagerFactory().getObject());
        return transactionManager;
    }
}
