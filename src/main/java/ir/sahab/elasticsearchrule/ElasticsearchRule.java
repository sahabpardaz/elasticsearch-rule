package ir.sahab.elasticsearchrule;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.cluster.ClusterName;
import org.elasticsearch.common.network.NetworkModule;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.reindex.ReindexPlugin;
import org.elasticsearch.node.InternalSettingsPreparer;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeValidationException;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.transport.Netty4Plugin;
import org.junit.rules.ExternalResource;

/**
 * A JUnit rule for starting an elasticsearch server on the local machine.
 */
public class ElasticsearchRule extends ExternalResource {

    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 9200;

    private Node server;
    private RestHighLevelClient restHighLevelClient;
    private int port;

    public ElasticsearchRule() {
        this(DEFAULT_PORT);
    }

    public ElasticsearchRule(int port) {
        this.port = port;
    }

    @Override
    protected void before() throws IOException, NodeValidationException, ExecutionException, InterruptedException {
        // Set up a setting for Elasticsearch server node.
        Settings.Builder builder = Settings.builder();
        builder.put(NetworkModule.TRANSPORT_TYPE_KEY, Netty4Plugin.NETTY_TRANSPORT_NAME);
        builder.put("node.id.seed", 0L);
        builder.put("node.name", "node" + new Random().nextInt(10000));
        builder.put(Environment.PATH_DATA_SETTING.getKey(), Files.createTempDirectory("elastic.data"));
        builder.put(Environment.PATH_HOME_SETTING.getKey(), Files.createTempDirectory("elastic.home"));
        builder.put(ClusterName.CLUSTER_NAME_SETTING.getKey(), "cluster-name");
        builder.put("discovery.type", "single-node");
        builder.put("http.port", port);
        Settings settings = builder.build();

        // Create the Elasticsearch server node and running it.
        // Netty4Plugin is necessary for making a TransportClient.
        // ReindexPlugin is necessary for making "delete by query" available.
        server = new TestNode(settings, Arrays.asList(Netty4Plugin.class, ReindexPlugin.class));
        server.start();
        server.client().admin().cluster().prepareHealth().setWaitForYellowStatus().execute().get();

        // Create a REST high level client ready to be used in tests.
        restHighLevelClient = new RestHighLevelClient(RestClient.builder(
                new HttpHost(DEFAULT_HOST, port, HttpHost.DEFAULT_SCHEME_NAME)));
    }

    @Override
    protected void after() {
        try {
            restHighLevelClient.close();
            server.close();
        } catch (IOException e) {
            throw new AssertionError("Cannot close the rest client or the server.");
        }
    }

    /**
     * A wrapper class for class org.elasticsearch.node.Node to make its constructor public.
     */
    public static class TestNode extends Node {
        private static final String DEFAULT_NODE_NAME = "mynode";

        public TestNode(Settings preparedSettings, Collection<Class<? extends Plugin>> classpathPlugins) {
			super(InternalSettingsPreparer.prepareEnvironment(preparedSettings, Collections.emptyMap(), null,
					() -> DEFAULT_NODE_NAME), classpathPlugins, false);
        }
    }

    public RestHighLevelClient getRestHighLevelClient() {
        return this.restHighLevelClient;
    }

    public String getRestAddress() {
        return DEFAULT_HOST + ":" + port;
    }
}