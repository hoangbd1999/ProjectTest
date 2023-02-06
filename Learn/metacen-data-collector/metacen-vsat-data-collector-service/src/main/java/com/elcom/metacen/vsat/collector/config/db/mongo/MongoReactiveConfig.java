package com.elcom.metacen.vsat.collector.config.db.mongo;

//package com.elcom.metacen.vsat.collector.config.db;
//
//import com.elcom.metacen.vsat.collector.config.db.converter.ZonedDateTimeReadConverter;
//import com.elcom.metacen.vsat.collector.config.db.converter.ZonedDateTimeWriteConverter;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
//import org.springframework.data.mongodb.core.ReactiveMongoOperations;
//import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
//import org.springframework.data.mongodb.core.SimpleReactiveMongoDatabaseFactory;
//import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
//import org.springframework.data.mongodb.core.convert.NoOpDbRefResolver;
//import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
//import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
//import com.mongodb.reactivestreams.client.MongoClient;
//import com.mongodb.reactivestreams.client.MongoClients;
//import java.util.ArrayList;
//import java.util.List;
//import org.springframework.context.annotation.Bean;
//import org.springframework.core.convert.converter.Converter;
//import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
//
//@Configuration
//@RequiredArgsConstructor
//@EnableReactiveMongoRepositories(basePackages = "com.elcom.metacen.vsat.collector")
//public class MongoReactiveConfig {
//
//    private final List<Converter<?, ?>> converters = new ArrayList<Converter<?, ?>>();
//
//    @Value("${spring.datasource.mongodb.uri}")
//    private String uri;
//
//    @Value("${spring.datasource.mongodb.database}")
//    private String database;
//
//    @Bean
//    public ReactiveMongoDatabaseFactory reactiveMongoDbFactory() {
//        return new SimpleReactiveMongoDatabaseFactory(this.reactiveMongoClient(), database);
//    }
//
////    @Bean
////    public ReactiveMongoOperations reactiveMongoTemplate() throws Exception {
////        return new ReactiveMongoTemplate(this.reactiveMongoDbFactory(), this.mappingMongoConverter());
////    }
//    
//    @Bean
//    public ReactiveMongoOperations reactiveMongoTemplate() throws Exception {
//        MappingMongoConverter converter = new MappingMongoConverter(
//                NoOpDbRefResolver.INSTANCE, new MongoMappingContext());
//        converter.setCustomConversions(this.customConversions());
//        // CALL THIS MANULLY, so that all the default convertors will be registered!
//        converter.afterPropertiesSet();
//        ReactiveMongoTemplate mongoTemplate = new ReactiveMongoTemplate(this.reactiveMongoDbFactory(), converter);
//        return mongoTemplate;
//    }
//
//    @Bean
//    public MongoClient reactiveMongoClient() {
//        return MongoClients.create(uri);
//    }
//
//    public String getMappingBasePackage() {
//        return "com.elcom.metacen.vsat.collector";
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
