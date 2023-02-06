package com.elcom.metacen.vsat.collector.config.db.mongo;

import com.mongodb.MongoClientURI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

@Configuration
public class MultipleMongoConfig {

    @Value("${spring.datasource.mongodb.collectorconfig.uri}")
    private String metacendbConfigUri;
    
    @Value("${spring.datasource.mongodb.contact.uri}")
    private String metacendbContactUri;
    
    @Primary
    @Bean(name = "newdb1Properties")
    public MongoProperties getNewCollectorConfigDbProps() throws Exception {
        return new MongoProperties();
    }
    
    @Bean(name = "newdb2Properties")
    public MongoProperties getNewContactConfigDbProps() throws Exception {
        return new MongoProperties();
    }

    @Primary
    @Bean(name = "collectorConfigDbMongoTemplate")
    public MongoTemplate collectorConfigDbMongoTemplate() throws Exception {
        return new MongoTemplate(newCollectorDbMongoDatabaseFactory());
    }

    @Bean(name = "contactDbMongoTemplate")
    public MongoTemplate contactDbMongoTemplate() throws Exception {
        return new MongoTemplate(newContactDbMongoDatabaseFactory());
    }

    @Primary
    @Bean
    public MongoDbFactory newCollectorDbMongoDatabaseFactory() throws Exception {
        MongoClientURI mcu = new MongoClientURI(this.metacendbConfigUri);
        return new SimpleMongoDbFactory(mcu);
    }

    @Bean
    public MongoDbFactory newContactDbMongoDatabaseFactory() throws Exception {
        MongoClientURI mcu = new MongoClientURI(this.metacendbContactUri);
        return new SimpleMongoDbFactory(mcu);
    }
}
