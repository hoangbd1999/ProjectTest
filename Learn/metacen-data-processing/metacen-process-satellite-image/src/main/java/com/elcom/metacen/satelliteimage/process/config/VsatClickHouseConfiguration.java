package com.elcom.metacen.satelliteimage.process.config;

import java.util.Properties;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;

@Configuration
@EnableJpaRepositories(
        entityManagerFactoryRef = "vsatChEntityManagerFactory",
        transactionManagerRef = "vsatChTransactionManager")
public class VsatClickHouseConfiguration {

    @Autowired
    private Environment env;

    @Bean(name = "vsatChDataSource")
    public DataSource chDatasource() {
        System.out.println("Loading config Clickhouse Datasource...");
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(env.getProperty("spring.datasource.click_house.driverClassName"));
        dataSource.setUrl(env.getProperty("spring.datasource.click_house.url"));
        dataSource.setUsername(env.getProperty("spring.datasource.click_house.username"));
        dataSource.setPassword(env.getProperty("spring.datasource.click_house.password"));
        return dataSource;
    }

    @Bean
    public JdbcTemplate chJDBC(DataSource chDatasource) {
        return new JdbcTemplate(chDatasource);
    }

    @Bean("clickHouseSession")
    public LocalSessionFactoryBean sessionFactory() {
        LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setDataSource(chDatasource());
        sessionFactory.setPackagesToScan(new String[]{"com.elcom"});
        sessionFactory.setHibernateProperties(hibernateProperties());
        return sessionFactory;
    }

    @Bean("clickHouseTransaction")
    public HibernateTransactionManager transactionManager() {
        HibernateTransactionManager transactionManager = new HibernateTransactionManager();
        transactionManager.setSessionFactory(sessionFactory().getObject());
        return transactionManager;
    }

    @SuppressWarnings("serial")
    private Properties hibernateProperties() {
        // Dùng dialect của MySQL áp dụng cho Clickhouse vì Clickhouse không có hibernate diaclect
        return new Properties() {
            {
                setProperty("hibernate.hbm2ddl.auto", "none");
                setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
                setProperty("hibernate.show_sql", "true");
            }
        };
    }
}
