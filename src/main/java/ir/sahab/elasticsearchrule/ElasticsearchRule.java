package ir.sahab.elasticsearchrule;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import org.elasticsearch.node.NodeValidationException;
import org.junit.rules.ExternalResource;

import java.io.IOException;

/**
 * A JUnit rule for starting an elasticsearch server on the local machine.
 */
public class ElasticsearchRule extends ExternalResource {

    private final ElasticsearchBase base;

    public ElasticsearchRule() {
        base = new ElasticsearchBase();
    }

    public ElasticsearchRule(int port) {
        base = new ElasticsearchBase(port);
    }

    @Override
    protected void before() throws IOException, NodeValidationException {
        base.setup();
    }

    @Override
    protected void after() {
        base.teardown();
    }

    public ElasticsearchClient getElasticsearchClient() {
        return base.getElasticsearchClient();
    }

    public String getHost() {
        return base.getHost();
    }

    public int getPort() {
        return base.getPort();
    }

    public void waitForGreenStatus(String... indices) {
        base.waitForGreenStatus(indices);
    }

}
