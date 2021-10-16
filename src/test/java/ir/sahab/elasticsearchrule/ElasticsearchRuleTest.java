package ir.sahab.elasticsearchrule;

import java.io.IOException;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

public class ElasticsearchRuleTest {

    @ClassRule
    public static final ElasticsearchRule elasticsearchRule = new ElasticsearchRule();

    private static RestHighLevelClient restHighLevelClient;

    @BeforeClass
    public static void setUpClass() {
        restHighLevelClient = elasticsearchRule.getRestHighLevelClient();
    }

    @Test
    public void testClient() throws IOException {
        String indexName = "twitter";
        CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName);
        CreateIndexResponse createIndexResponse = restHighLevelClient.indices().create(createIndexRequest,
                RequestOptions.DEFAULT);
        Assert.assertTrue(createIndexResponse.isAcknowledged());

        ClusterHealthRequest clusterHealthRequest = new ClusterHealthRequest(indexName);
        clusterHealthRequest.waitForYellowStatus();
        restHighLevelClient.cluster().health(clusterHealthRequest, RequestOptions.DEFAULT);

        IndexRequest indexRequest = new IndexRequest(indexName);
        indexRequest.id("1");
        String json = "{"
                + "    \"user\":\"kimchy\","
                + "    \"postDate\":\"2013-01-30\","
                + "    \"message\":\"trying out Elasticsearch\""
                + "}";
        indexRequest.source(json, XContentType.JSON);
        IndexResponse response = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
        Assert.assertEquals(RestStatus.CREATED, response.status());

        RefreshRequest refreshRequest = new RefreshRequest(indexName);
        restHighLevelClient.indices().refresh(refreshRequest, RequestOptions.DEFAULT);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery("user", "kimchy"));
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        Assert.assertEquals(1, searchResponse.getHits().getHits().length);
        String postDate = (String) searchResponse.getHits().getAt(0).getSourceAsMap().get("message");
        Assert.assertEquals("trying out Elasticsearch", postDate);
    }

    @Test
    public void testHostAndPort() throws IOException {
        String elasticsearchHost = elasticsearchRule.getHost();
        int elasticsearchPort = elasticsearchRule.getPort();
        try (RestHighLevelClient anotherRestHighLevelClient = new RestHighLevelClient(RestClient.builder(
            new HttpHost(elasticsearchHost, elasticsearchPort, "http")))) {
            String indexName = "twitter2";
            CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName);
            CreateIndexResponse createIndexResponse = anotherRestHighLevelClient.indices().create(createIndexRequest,
                    RequestOptions.DEFAULT);
            Assert.assertTrue(createIndexResponse.isAcknowledged());
        }
    }
}