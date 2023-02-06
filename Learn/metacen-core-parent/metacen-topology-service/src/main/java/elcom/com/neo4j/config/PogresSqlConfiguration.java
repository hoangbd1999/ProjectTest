package elcom.com.neo4j.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;

/**
 *
 * @author ducnh
 */
@Configuration
@EnableJpaRepositories(basePackages = "elcom.com.neo4j.repositoryPostgre",
        entityManagerFactoryRef = "itsCoreEntityManagerFactory",
        transactionManagerRef = "itsCoreTransactionManager")
public class PogresSqlConfiguration {

    @Autowired
    private Environment env;

    @Primary
    @Bean(name = "itsCoreDataSource")
    public DataSource itsCoreDataSource() {
        System.out.println(" load config itsCoreDataSource");
//        final DriverManagerDataSource dataSource = new DriverManagerDataSource();
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName(env.getProperty("spring.datasource.postgres.driverClassName"));
//        dataSource.setUrl(env.getProperty("spring.datasource.postgres.url"));
        dataSource.setJdbcUrl(env.getProperty("spring.datasource.postgres.url"));
        dataSource.setUsername(env.getProperty("spring.datasource.postgres.username"));
        dataSource.setPassword(env.getProperty("spring.datasource.postgres.password"));
        dataSource.addDataSourceProperty("cachePrepStmts", true);
        dataSource.addDataSourceProperty("prepStmtCacheSize", 25000);
        dataSource.addDataSourceProperty("prepStmtCacheSqlLimit", 20048);
        dataSource.addDataSourceProperty("useServerPrepStmts", true);
        dataSource.addDataSourceProperty("initializationFailFast", true);
        dataSource.setPoolName("DS1_HIKARICP_CONNECTION_POOL");

//        dataSource.setConnectionProperties(hibernateProperties());

        return dataSource;
    }

    @Primary
    @Bean(name = "itsCoreEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean itsCoreEntityManagerFactory() {
        final LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(itsCoreDataSource());
        em.setPackagesToScan("elcom.com.neo4j.model");

        final HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        final HashMap<String, Object> properties = new HashMap<>();
//        properties.put("hibernate.hbm2ddl.auto", env.getProperty("hibernate.hbm2ddl.auto"));
//        properties.put("hibernate.dialect", env.getProperty("hibernate.dialect"));
        em.setJpaPropertyMap(properties);

        return em;
    }

    @Primary
    @Bean
    public PlatformTransactionManager itsCoreTransactionManager() {
        final JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(itsCoreEntityManagerFactory().getObject());
        return transactionManager;
    }
}