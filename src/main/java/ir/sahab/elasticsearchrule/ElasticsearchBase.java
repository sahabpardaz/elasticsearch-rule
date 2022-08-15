package ir.sahab.elasticsearchrule;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.HealthStatus;
import co.elastic.clients.elasticsearch.cluster.HealthResponse;
import co.elastic.clients.elasticsearch.indices.PutTemplateRequest;
import co.elastic.clients.elasticsearch.indices.PutTemplateResponse;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import ir.sahab.cleanup.Cleanups;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.cluster.ClusterName;
import org.elasticsearch.cluster.health.ClusterHealthStatus;
import org.elasticsearch.common.network.NetworkModule;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.node.InternalSettingsPreparer;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeValidationException;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.reindex.ReindexPlugin;
import org.elasticsearch.transport.Netty4Plugin;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

class ElasticsearchBase {

    private static final String DEFAULT_HOST = "localhost";

    private final int port;

    private Path ruleTempDirectory;
    private Node server;
    private ElasticsearchClient elasticsearchClient;
    private ElasticsearchTransport transport;

    public ElasticsearchBase() {
        this(anOpenPort());
    }

    public ElasticsearchBase(int port) {
        this.port = port;
    }

    static Integer anOpenPort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new AssertionError("Unable to find an open port.", e);
        }
    }

    void setup() throws IOException, NodeValidationException {
        ruleTempDirectory = Files.createTempDirectory("elasticsearch-junit-rule");

        // Set up a setting for Elasticsearch server node.
        Settings.Builder builder = Settings.builder();
        builder.put(NetworkModule.TRANSPORT_TYPE_KEY, Netty4Plugin.NETTY_TRANSPORT_NAME);
        builder.put("node.id.seed", 0L);
        builder.put("node.name", "node1");
        builder.put(Environment.PATH_DATA_SETTING.getKey(), ruleTempDirectory.resolve("elastic-data"));
        builder.put(Environment.PATH_HOME_SETTING.getKey(), ruleTempDirectory.resolve("elastic-home"));
        builder.put(ClusterName.CLUSTER_NAME_SETTING.getKey(), "cluster-name");
        builder.put("discovery.type", "single-node");
        builder.put("http.port", port);
        Settings settings = builder.build();

        // Create the Elasticsearch server node and running it.
        // Netty4Plugin is necessary for making a TransportClient.
        // ReindexPlugin is necessary for making "delete by query" available.
        server = new TestNode(settings, Arrays.asList(Netty4Plugin.class, ReindexPlugin.class));
        server.start();
        ClusterHealthResponse clusterHealthResponse = server.client().admin().cluster().prepareHealth().setWaitForGreenStatus().get();
        if (clusterHealthResponse.getStatus() != ClusterHealthStatus.GREEN) {
            throw new AssertionError("The state of the cluster did not change to green.");
        }

        // Create a ElasticSearch client ready to be used in tests.

        RestClient restClient = RestClient.builder(new HttpHost(DEFAULT_HOST, port, HttpHost.DEFAULT_SCHEME_NAME)).build();
        transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        elasticsearchClient = new ElasticsearchClient(transport);

        // By default, every index that is created has 1 shards and 1 replica.
        // However, the rule provides only a single node cluster. In order to change them,
        // a template is created that is used by default for all indexes created.

        final PutTemplateRequest request = new PutTemplateRequest.Builder().name("default-junit-rule-template").indexPatterns("*").order(-1).settings("index.number_of_shards", JsonData.of(1)).settings("index.number_of_replicas", JsonData.of(0)).build();
        final PutTemplateResponse putTemplateResponse = elasticsearchClient.indices().putTemplate(request);
        if (!putTemplateResponse.acknowledged()) {
            throw new AssertionError("Adding the default template has encountered an error.");
        }
    }

    void teardown() {
        try {
            Cleanups.of(transport, server).and(() -> Files.walk(ruleTempDirectory).sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    throw new AssertionError("Unable to remove temporary file: " + path, e);
                }
            })).doAll();
        } catch (IOException e) {
            throw new AssertionError("Unable to close resources", e);
        }
    }


    public ElasticsearchClient getElasticsearchClient() {
        return this.elasticsearchClient;
    }

    public String getHost() {
        return DEFAULT_HOST;
    }

    public int getPort() {
        return this.port;
    }

    public void waitForGreenStatus(String... indices) {
        try {
            final HealthResponse healthResponse = elasticsearchClient.cluster().health();
            if (healthResponse.status() != HealthStatus.Green) {
                throw new AssertionError("The state of the indices did not change to green: " + Arrays.toString(indices));
            }
        } catch (IOException e) {
            throw new AssertionError("Unable to retrieve status of indices: " + Arrays.toString(indices), e);
        }
    }

    /**
     * A wrapper class for class org.elasticsearch.node.Node to make its constructor public.
     */
    private static class TestNode extends Node {

        private static final String DEFAULT_NODE_NAME = "test-node";

        public TestNode(Settings preparedSettings, Collection<Class<? extends Plugin>> classpathPlugins) {
            super(InternalSettingsPreparer.prepareEnvironment(preparedSettings, Collections.emptyMap(), null, () -> DEFAULT_NODE_NAME), classpathPlugins, false);
        }
    }

}
