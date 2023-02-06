/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.group.detect.repository.clickhouse;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

/**
 *
 * @author Admin
 */
@Repository
public class BaseClickHouseRepository {

    @Value("${clickhouse.schemaMeta}")
    private String schemaMeta;

    protected SessionFactory sessionFactory;
    protected DataSource clickHouseDataSource;

    protected BaseClickHouseRepository(EntityManagerFactory factory, DataSource clickHouseDataSource) {
        if (factory.unwrap(SessionFactory.class) == null) {
            throw new NullPointerException("factory is not a hibernate factory");
        }

        this.sessionFactory = factory.unwrap(SessionFactory.class);
        this.clickHouseDataSource = clickHouseDataSource;
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

    public String getSchemaMeta() {
        return schemaMeta;
    }

    public void setSchemaMeta(String schemaMeta) {
        this.schemaMeta = schemaMeta;
    }
}
