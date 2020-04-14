package ir.sahab.elasticsearchrule;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import org.elasticsearch.cli.Terminal;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.ClusterName;
import org.elasticsearch.common.network.NetworkModule;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.reindex.ReindexPlugin;
import org.elasticsearch.node.InternalSettingsPreparer;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeValidationException;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.transport.Netty4Plugin;
import org.elasticsearch.transport.TransportService;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.junit.rules.ExternalResource;

/**
 * A JUnit rule for starting an elasticsearch server on the local machine.
 */
public class ElasticsearchRule extends ExternalResource {

    private TransportClient transportClient;
    private TransportAddress transportAddress;
    private String clusterName;
    private Node server;

    public ElasticsearchRule(String clusterName) {
        this.clusterName = clusterName;
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
        builder.put(ClusterName.CLUSTER_NAME_SETTING.getKey(), clusterName);
        builder.put("discovery.type", "single-node");
        Settings settings = builder.build();

        // Create the Elasticsearch server node and running it.
        // Netty4Plugin is necessary for making a TransportClient.
        // ReindexPlugin is necessary for making "delete by query" available.
        server = new TestNode(settings, Arrays.asList(Netty4Plugin.class, ReindexPlugin.class));
        server.start();
        server.client().admin().cluster().prepareHealth().setWaitForYellowStatus().execute().get();

        // Create a transport client ready to be used in tests.
        transportAddress = server.injector().getInstance(TransportService.class).boundAddress().publishAddress();
        transportClient = new PreBuiltTransportClient(server.settings());
        transportClient.addTransportAddress(transportAddress);
    }

    @Override
    protected void after() {
        transportClient.close();
        try {
            server.close();
        } catch (IOException e) {
            throw new AssertionError("Cannot close the server.");
        }
    }

    /**
     * A wrapper class for class org.elasticsearch.node.Node to make its constructor public.
     */
    public static class TestNode extends Node {
        public TestNode(Settings preparedSettings, Collection<Class<? extends Plugin>> classpathPlugins) {
            super(InternalSettingsPreparer.prepareEnvironment(preparedSettings, Terminal.DEFAULT,
                    Collections.emptyMap(), null), classpathPlugins);
        }
    }

    public TransportClient getTransportClient() {
        return transportClient;
    }

    public String getAddress() {
        return transportAddress.toString();
    }
}