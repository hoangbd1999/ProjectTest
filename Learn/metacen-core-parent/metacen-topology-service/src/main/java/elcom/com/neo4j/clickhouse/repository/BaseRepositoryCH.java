package elcom.com.neo4j.clickhouse.repository;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

/**
 *
 * @author anhdv
 */
@Repository
public class BaseRepositoryCH {

    protected SessionFactory sessionFactory;
    protected DataSource clDatasource;

    protected BaseRepositoryCH(EntityManagerFactory factory, DataSource clDataSource) {
        if (factory.unwrap(SessionFactory.class) == null) {
            throw new NullPointerException("factory is not a hibernate factory");
        }

        this.sessionFactory = factory.unwrap(SessionFactory.class);
        this.clDatasource=clDataSource;
    }

    protected Session openSession() {
        Session session = this.sessionFactory.openSession();
        return session;
    }

    protected void closeSession(Session session) {
        if (session != null && session.isOpen()) {
            session.disconnect();
            session.close();
        }
    }
}
