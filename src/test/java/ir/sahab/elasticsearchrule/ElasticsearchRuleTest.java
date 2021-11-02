package ir.sahab.elasticsearchrule;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

public class ElasticsearchRuleTest {

    private static final String ELASTICSEARCH_CLUSTER_NAME = "elasticsearch";

    @ClassRule
    public static final ElasticsearchRule elasticsearchRule = new ElasticsearchRule(ELASTICSEARCH_CLUSTER_NAME);

    private static TransportClient transportClient;

    @BeforeClass
    public static void setUpClass() {
        transportClient = elasticsearchRule.getTransportClient();
    }

    @Test
    public void testClient() {
        String indexName = "twitter";
        CreateIndexResponse createIndexResponse = transportClient.admin().indices().prepareCreate(indexName).get();
        Assert.assertTrue(createIndexResponse.isAcknowledged());

        ClusterHealthResponse clusterHealthResponse =
                transportClient.admin().cluster().prepareHealth().setWaitForGreenStatus().get();
        if (clusterHealthResponse.status() == RestStatus.REQUEST_TIMEOUT) {
            throw new AssertionError("The state of the system did not change to green.");
        }

        String json = "{"
                + "    \"user\":\"kimchy\","
                + "    \"postDate\":\"2013-01-30\","
                + "    \"message\":\"trying out Elasticsearch\""
                + "}";
        IndexResponse response = transportClient.prepareIndex("twitter", "tweet")
                .setSource(json, XContentType.JSON)
                .get();
        Assert.assertEquals(RestStatus.CREATED, response.status());
        transportClient.admin().indices().prepareRefresh(indexName).get();

        SearchResponse searchResponse = transportClient.prepareSearch(indexName)
                .setQuery(QueryBuilders.matchQuery("user", "kimchy"))
                .get();
        Assert.assertEquals(1, searchResponse.getHits().getHits().length);
        String postDate = (String) searchResponse.getHits().getAt(0).getSourceAsMap().get("message");
        Assert.assertEquals("trying out Elasticsearch", postDate);
    }

    @Test
    public void testAddress() {
        String address = elasticsearchRule.getAddress();
        String elasticsearchHost = address.split(":")[0];
        int elasticsearchPort = Integer.parseInt(address.split(":")[1]);
        InetAddress elasticsearchInetAddress;
        try {
            elasticsearchInetAddress = InetAddress.getByName(elasticsearchHost);
        } catch (UnknownHostException e) {
            throw new AssertionError("Cannot get the elasticsearch server address: " + elasticsearchHost, e);
        }
        Settings settings = Settings.builder().put("cluster.name", ELASTICSEARCH_CLUSTER_NAME).build();
        TransportClient internalTransportClient = new PreBuiltTransportClient(settings);
        internalTransportClient.addTransportAddress(new TransportAddress(elasticsearchInetAddress, elasticsearchPort));
        String indexName = "twitter2";
        CreateIndexResponse createIndexResponse = internalTransportClient.admin().indices()
                .prepareCreate(indexName).get();
        Assert.assertTrue(createIndexResponse.isAcknowledged());
        internalTransportClient.close();
    }
}