package com.elcom.metacen.content.repository;

import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.elcom.metacen.content.model.VsatMediaAnalyzed;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Repository;

import static org.springframework.data.elasticsearch.client.elc.QueryBuilders.queryStringQuery;
import static org.springframework.data.elasticsearch.client.elc.QueryBuilders.termQuery;

@Repository
public class VsatMediaAnalyzedRepositoryCustomer {
    private static final String PRODUCT_INDEX = "media_analyzed";

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    public SearchHits<VsatMediaAnalyzed> findProductsByBrand() {

        QueryBuilder queryBuilder = QueryBuilders
                .boolQuery()
                .should(QueryBuilders.matchQuery("sourceMac","*00:40:FD:01:C0:F4*"))
                .should(QueryBuilders.matchQuery("sourceMac","00:40:FD:01:C0:F4"))
                .should(QueryBuilders.matchQuery("fileContentUtf8","*00:40:FD:01:C0:F4*"))
                .minimumShouldMatch(1);
        Query searchQuery = new NativeSearchQueryBuilder()
                .withQuery(queryBuilder)
                .build();

        SearchHits<VsatMediaAnalyzed> productHits =
                elasticsearchOperations
                        .search(searchQuery,
                                VsatMediaAnalyzed.class,
                                IndexCoordinates.of(PRODUCT_INDEX));
        return productHits;

    }
//    public SearchHits<EmailAnalyzed> findMail() {
//
//
//        QueryBuilder q1 = QueryBuilders.matchQuery("mediaTypeName", "Audio");
//        QueryBuilder queryBuilder = QueryBuilders
//                .boolQuery().must(QueryBuilders.matchQuery("mediaTypeName", "Email"))
//                .should(QueryBuilders.matchQuery("to.name", "*hungnk*"))
//                .should(QueryBuilders.matchQuery("to.address", "*guohaiminfu@163.com*"))
//                .minimumShouldMatch(1);
////        QueryStringQueryBuilder query = QueryBuilders.queryStringQuery(queryBuilder.toString()).allowLeadingWildcard(true).analyzeWildcard(true);
////        QueryBuilder queryBuilder =
////                QueryBuilders
////                        .matchQuery("fileContentUtf8", "testNoidung");
//
//        Query searchQuery = new NativeSearchQueryBuilder()
//                .withQuery(queryBuilder)
//                .build();
//
//        SearchHits<EmailAnalyzed> productHits =
//                elasticsearchOperations
//                        .search(searchQuery,
//                                EmailAnalyzed.class,
//                                IndexCoordinates.of("indexmailanalyzed"));
//        return productHits;
//
//    }
}
