package com.elcom.metacen.vsat.collector.config.db.mongo;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = {"com.elcom.metacen.vsat.collector.repository.mongodb.dbcontact"},
        mongoTemplateRef = ContactDbConfig.MONGO_TEMPLATE
)
public class ContactDbConfig {

    protected static final String MONGO_TEMPLATE = "contactDbMongoTemplate";
}
