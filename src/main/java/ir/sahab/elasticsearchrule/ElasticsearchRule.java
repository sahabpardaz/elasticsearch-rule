package ir.sahab.elasticsearchrule;

import ir.sahab.cleanup.Cleanups;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.ExecutionException;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateRequest;
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateResponse;
import org.elasticsearch.cli.Terminal;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.ClusterName;
import org.elasticsearch.cluster.health.ClusterHealthStatus;
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

    private final String clusterName;

    private Path ruleTempDirectory;
    private TransportClient transportClient;
    private TransportAddress transportAddress;
    private Node server;

    public ElasticsearchRule(String clusterName) {
        this.clusterName = clusterName;
    }

    @Override
    protected void before() throws IOException, NodeValidationException, ExecutionException, InterruptedException {
        ruleTempDirectory = Files.createTempDirectory("elasticsearch-junit-rule");

        // Set up a setting for Elasticsearch server node.
        Settings.Builder builder = Settings.builder();
        builder.put(NetworkModule.TRANSPORT_TYPE_KEY, Netty4Plugin.NETTY_TRANSPORT_NAME);
        builder.put("node.id.seed", 0L);
        builder.put("node.name", "node1");
        builder.put(Environment.PATH_DATA_SETTING.getKey(), ruleTempDirectory.resolve("elastic-data"));
        builder.put(Environment.PATH_HOME_SETTING.getKey(), ruleTempDirectory.resolve("elastic-home"));
        builder.put(ClusterName.CLUSTER_NAME_SETTING.getKey(), clusterName);
        builder.put("discovery.type", "single-node");
        Settings settings = builder.build();

        // Create the Elasticsearch server node and running it.
        // Netty4Plugin is necessary for making a TransportClient.
        // ReindexPlugin is necessary for making "delete by query" available.
        server = new TestNode(settings, Arrays.asList(Netty4Plugin.class, ReindexPlugin.class));
        server.start();
        ClusterHealthResponse clusterHealthResponse = server.client().admin().cluster().prepareHealth()
                .setWaitForGreenStatus().get();
        if (clusterHealthResponse.getStatus() != ClusterHealthStatus.GREEN) {
            throw new AssertionError("The state of the cluster did not change to green.");
        }

        // Create a transport client ready to be used in tests.
        transportAddress = server.injector().getInstance(TransportService.class).boundAddress().publishAddress();
        transportClient = new PreBuiltTransportClient(server.settings());
        transportClient.addTransportAddress(transportAddress);

        // By default, every index that is created has 5 shards and 1 replica.
        // However, the rule provides only a single node cluster. In order to change them,
        // a template is created that is used by default for all indexes created.
        PutIndexTemplateRequest request = new PutIndexTemplateRequest("default-junit-rule-template");
        request.patterns(Collections.singletonList("*"));
        request.order(-1);
        request.settings(Settings.builder()
                .put("index.number_of_shards", 1)
                .put("index.number_of_replicas", 0)
        );
        PutIndexTemplateResponse putTemplateResponse = transportClient.admin().indices().putTemplate(request).get();
        if (!putTemplateResponse.isAcknowledged()) {
            throw new AssertionError("Adding the default template has encountered an error.");
        }
    }

    @Override
    protected void after() {
        try {
            Cleanups.of(transportClient, server)
                    .and(() -> Files.walk(ruleTempDirectory)
                            .sorted(Comparator.reverseOrder())
                            .forEach(path -> {
                                try {
                                    Files.deleteIfExists(path);
                                } catch (IOException e) {
                                    throw new AssertionError("Unable to remove temporary file: " + path, e);
                                }
                            }))
                    .doAll();
        } catch (IOException e) {
            throw new AssertionError("Unable to close resources", e);
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

    public void waitForGreenStatus() {
        ClusterHealthResponse clusterHealthResponse = transportClient.admin().cluster().prepareHealth()
                .setWaitForGreenStatus().get();
        if (clusterHealthResponse.getStatus() != ClusterHealthStatus.GREEN) {
            throw new AssertionError("The state of the cluster did not change to green.");
        }
    }
}