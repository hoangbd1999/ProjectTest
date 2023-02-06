/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.mapping.data.repository;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author Admin
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
