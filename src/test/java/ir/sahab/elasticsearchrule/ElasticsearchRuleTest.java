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
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;

public class ElasticsearchRuleTest {

    @ClassRule
    public static final ElasticsearchRule elasticsearchRule = new ElasticsearchRule();

    private static ElasticsearchClient elasticsearchClient;

    @BeforeClass
    public static void setUpClass() {
        elasticsearchClient = elasticsearchRule.getElasticsearchClient();
    }

    @Test
    public void testClient() throws IOException {
        String indexName = "twitter";
        CreateIndexRequest createIndexRequest = new CreateIndexRequest.Builder()
                .index(indexName)
                .build();
        CreateIndexResponse createIndexResponse = elasticsearchClient.indices().create(createIndexRequest);
        Assert.assertTrue(createIndexResponse.acknowledged());
        elasticsearchRule.waitForGreenStatus(indexName);

        final Tweet tweet = new Tweet("kimchy", "2022-01-30", "trying out Elasticsearch");

        final IndexRequest<Tweet> indexRequest = new IndexRequest.Builder<Tweet>()
                .index(indexName)
                .id("1")
                .document(tweet)
                .build();
        final IndexResponse response = elasticsearchClient.index(indexRequest);
        Assert.assertEquals(Result.Created, response.result());

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
        Assert.assertEquals(1, searchResponse.hits().hits().size());
        String message = searchResponse.hits().hits().get(0).source().message;
        Assert.assertEquals("trying out Elasticsearch", message);
    }

    @Test
    public void testHostAndPort() throws IOException {
        String elasticsearchHost = elasticsearchRule.getHost();
        int elasticsearchPort = elasticsearchRule.getPort();
        try (
                final RestClient restClient = RestClient.builder(new HttpHost(elasticsearchHost, elasticsearchPort, "http")).build();
                final RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper())
        ) {
            ElasticsearchClient anotherRestHighLevelClient = new ElasticsearchClient(transport);
            String indexName = "twitter2";
            CreateIndexRequest createIndexRequest = new CreateIndexRequest.Builder().index(indexName).build();
            CreateIndexResponse createIndexResponse = anotherRestHighLevelClient.indices().create(createIndexRequest);
            Assert.assertTrue(createIndexResponse.acknowledged());
        }
    }

    public static class Tweet {
        private String user;
        private String postDate;
        private String message;

        public Tweet(String user, String postDate, String message) {
            this.user = user;
            this.postDate = postDate;
            this.message = message;
        }

        public Tweet() {
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getPostDate() {
            return postDate;
        }

        public void setPostDate(String postDate) {
            this.postDate = postDate;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

}
