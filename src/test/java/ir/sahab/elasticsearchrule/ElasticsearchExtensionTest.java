package ir.sahab.elasticsearchrule;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Result;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.RefreshRequest;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import ir.sahab.elasticsearchrule.common.Tweet;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ElasticsearchExtensionTest {


    @RegisterExtension
    static ElasticsearchExtension elasticsearchExtension = new ElasticsearchExtension();
    static ElasticsearchClient elasticsearchClient;

    @BeforeAll
    static void setUpClass() {
        elasticsearchClient = elasticsearchExtension.getElasticsearchClient();
    }

    @Test
    void testClient() throws IOException {
        String indexName = "twitter";
        CreateIndexRequest createIndexRequest = new CreateIndexRequest.Builder()
                .index(indexName)
                .build();
        CreateIndexResponse createIndexResponse = elasticsearchClient.indices().create(createIndexRequest);
        assertTrue(createIndexResponse.acknowledged());
        elasticsearchExtension.waitForGreenStatus(indexName);

        final Tweet tweet = new Tweet("kimchy", "2022-01-30", "trying out Elasticsearch");

        final IndexRequest<Tweet> indexRequest = new IndexRequest.Builder<Tweet>()
                .index(indexName)
                .id("1")
                .document(tweet)
                .build();
        final IndexResponse response = elasticsearchClient.index(indexRequest);
        assertEquals(Result.Created, response.result());

        co.elastic.clients.elasticsearch.indices.RefreshRequest refreshRequest = new RefreshRequest.Builder()
                .index(indexName).build();
        elasticsearchClient.indices().refresh(refreshRequest);

        final Query query = new Query.Builder()
                .match(new MatchQuery.Builder().field("user").query("kimchy").build())
                .build();
        final SearchRequest searchRequest = new SearchRequest.Builder()
                .query(query)
                .index(indexName)
                .build();
        final SearchResponse<Tweet> searchResponse = elasticsearchClient.search(searchRequest, Tweet.class);
        assertEquals(1, searchResponse.hits().hits().size());
        final Tweet foundTweet = searchResponse.hits().hits().get(0).source();
        assertNotNull(foundTweet);
        String message = foundTweet.getMessage();
        assertEquals("trying out Elasticsearch", message);
    }

    @Test
    void testHostAndPort() throws IOException {
        String elasticsearchHost = elasticsearchExtension.getHost();
        int elasticsearchPort = elasticsearchExtension.getPort();
        final RestClient restClient = RestClient.builder(new HttpHost(elasticsearchHost, elasticsearchPort, "http")).build();
        try (
                final RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper())
        ) {
            ElasticsearchClient anotherElasticsearchClient = new ElasticsearchClient(transport);
            String indexName = "twitter2";
            CreateIndexRequest createIndexRequest = new CreateIndexRequest.Builder().index(indexName).build();
            CreateIndexResponse createIndexResponse = anotherElasticsearchClient.indices().create(createIndexRequest);
            assertTrue(createIndexResponse.acknowledged());
        }
    }

}
