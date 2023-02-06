package elcom.com.neo4j.clickhouse.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableJpaRepositories(
        entityManagerFactoryRef = "ChEntityManagerFactory",
        transactionManagerRef = "ChTransactionManager"
)
public class ClickHouseConfig {

    @Autowired
    private Environment env;


    @Bean(name = "chDatasource")
    public DataSource chDatasource() {
        System.out.println(" load config chDataSource");
//        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName(env.getProperty("spring.datasource.click_house.driverClassName"));
        dataSource.setJdbcUrl(env.getProperty("spring.datasource.click_house.url"));
        dataSource.setUsername(env.getProperty("spring.datasource.click_house.username"));
        dataSource.setPassword(env.getProperty("spring.datasource.click_house.password"));
        dataSource.addDataSourceProperty("cachePrepStmts", true);
        dataSource.addDataSourceProperty("prepStmtCacheSize", 25000);
        dataSource.addDataSourceProperty("prepStmtCacheSqlLimit", 20048);
        dataSource.addDataSourceProperty("useServerPrepStmts", true);
        dataSource.addDataSourceProperty("initializationFailFast", true);
        dataSource.setPoolName("DS1_HIKARICP_CONNECTION_POOL");
        return dataSource;
    }

    @Bean("clickHouseSession")
    public LocalSessionFactoryBean sessionFactory() {
        LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setDataSource(chDatasource());
        sessionFactory.setPackagesToScan(new String[] { "movie.spring.data.neo4j" });
        sessionFactory.setHibernateProperties(hibernateProperties());
        return sessionFactory;
    }

    @Bean
    public JdbcTemplate chJDBC(DataSource chDatasource) {
        return new JdbcTemplate(chDatasource);
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
                setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
                setProperty("hibernate.show_sql", "true");
            }
        };
    }
}