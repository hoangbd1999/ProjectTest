package com.elcom.metacen.saga.repository;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManagerFactory;

/**
 *
 * @author hoangbd
 */
@Repository
public class BaseRepository {

    protected SessionFactory sessionFactory;

    @Value("${postgres.currentSchema}")
    private String schema;

    protected BaseRepository(EntityManagerFactory factory) {
        if (factory.unwrap(SessionFactory.class) == null) {
            throw new NullPointerException("factory is not a hibernate factory");
        }

        this.sessionFactory = factory.unwrap(SessionFactory.class);
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

    protected String getSchema() {
        return schema;
    }

    protected void setSchema(String schema) {
        this.schema = schema;
    }
}
