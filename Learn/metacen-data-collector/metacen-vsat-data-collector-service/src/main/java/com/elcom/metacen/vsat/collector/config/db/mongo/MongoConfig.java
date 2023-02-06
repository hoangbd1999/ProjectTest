package com.elcom.metacen.vsat.collector.config.db.mongo;

//package com.elcom.metacen.vsat.collector.config.db;
//
//import com.elcom.metacen.vsat.collector.config.db.converter.ZonedDateTimeReadConverter;
//import com.elcom.metacen.vsat.collector.config.db.converter.ZonedDateTimeWriteConverter;
//import com.mongodb.ConnectionString;
//import com.mongodb.MongoClientSettings;
//import com.mongodb.client.MongoClient;
//import com.mongodb.client.MongoClients;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.ComponentScan;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.convert.converter.Converter;
//import org.springframework.data.mongodb.core.MongoTemplate;
//import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
//import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
//import java.util.ArrayList;
//import java.util.List;
//
//@Configuration
//@ComponentScan(basePackages = {"com.elcom.metacen.vsat.collector.repositorymongo"})
//@EnableMongoRepositories(basePackages = {"com.elcom.metacen.vsat.collector.repositorymongo"})
//public class MongoConfig {
//
//    private final List<Converter<?, ?>> converters = new ArrayList<Converter<?, ?>>();
//
//    @Value("${spring.datasource.mongodb.uri}")
//    private String uri;
//
//    @Value("${spring.datasource.mongodb.database}")
//    private String database;
//
//    public String getMappingBasePackage() {
//        return "com.elcom.metacen.vsat.collector.repositorymongo";
//    }
//
//    @Bean
//    public MongoClient mongoClient() {
//        ConnectionString connectionString = new ConnectionString(uri);
//        MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
//                .applyConnectionString(connectionString)
//                .build();
//
//        return MongoClients.create(mongoClientSettings);
//    }
//
//    @Bean
//    public MongoTemplate mongoTemplate() throws Exception {
//        MongoTemplate template = new MongoTemplate(mongoClient(), database);
//        return template;
//    }
//
//    @Bean
//    @ConditionalOnMissingBean(MongoCustomConversions.class)
//    public MongoCustomConversions customConversions() {
//        converters.add(new ZonedDateTimeReadConverter());
//        converters.add(new ZonedDateTimeWriteConverter());
//
//        return new MongoCustomConversions(converters);
//    }
//}
