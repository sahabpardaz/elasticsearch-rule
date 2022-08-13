package ir.sahab.elasticsearchrule;

import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.health.ClusterHealthStatus;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ElasticsearchExtensionTest {
    private static final String ELASTICSEARCH_CLUSTER_NAME = "elasticsearch";

    @RegisterExtension
    static ElasticsearchExtension elasticsearchExtension = new ElasticsearchExtension(ELASTICSEARCH_CLUSTER_NAME);
    private static TransportClient transportClient;

    @BeforeAll
    static void setUpClass() {
        transportClient = elasticsearchExtension.getTransportClient();
    }


    @Test
    void testClient() {
        String indexName = "twitter";
        CreateIndexResponse createIndexResponse = transportClient.admin().indices().prepareCreate(indexName).get();
        assertTrue(createIndexResponse.isAcknowledged());
        elasticsearchExtension.waitForGreenStatus();

        String json = "{"
                + "    \"user\":\"kimchy\","
                + "    \"postDate\":\"2013-01-30\","
                + "    \"message\":\"trying out Elasticsearch\""
                + "}";
        IndexResponse response = transportClient.prepareIndex(indexName, "tweet")
                .setSource(json, XContentType.JSON)
                .get();
        assertEquals(RestStatus.CREATED, response.status());
        transportClient.admin().indices().prepareRefresh(indexName).get();

        SearchResponse searchResponse = transportClient.prepareSearch(indexName)
                .setQuery(QueryBuilders.matchQuery("user", "kimchy"))
                .get();
        assertEquals(1, searchResponse.getHits().getHits().length);
        String postDate = (String) searchResponse.getHits().getAt(0).getSourceAsMap().get("message");
        assertEquals("trying out Elasticsearch", postDate);
    }

    @Test
    void testAddress() {
        String address = elasticsearchExtension.getAddress();
        String elasticsearchHost = address.split(":")[0];
        int elasticsearchPort = Integer.parseInt(address.split(":")[1]);
        InetAddress elasticsearchInetAddress;
        try {
            elasticsearchInetAddress = InetAddress.getByName(elasticsearchHost);
        } catch (UnknownHostException e) {
            throw new AssertionError("Cannot get the elasticsearch server address: " + elasticsearchHost, e);
        }
        Settings settings = Settings.builder().put("cluster.name", ELASTICSEARCH_CLUSTER_NAME).build();
        try (TransportClient internalClient = new PreBuiltTransportClient(settings)) {
            internalClient.addTransportAddress(new TransportAddress(elasticsearchInetAddress, elasticsearchPort));

            String indexName = "twitter2";
            CreateIndexResponse createIndexResponse = internalClient.admin().indices().prepareCreate(indexName).get();
            assertTrue(createIndexResponse.isAcknowledged());
            ClusterHealthResponse clusterHealthResponse = internalClient.admin().cluster().prepareHealth()
                    .setWaitForGreenStatus().get();
            if (clusterHealthResponse.getStatus() != ClusterHealthStatus.GREEN) {
                throw new AssertionError("The state of the cluster did not change to green.");
            }
        }
    }
}
