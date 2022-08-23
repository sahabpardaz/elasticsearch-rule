package ir.sahab.elasticsearchrule;

import org.elasticsearch.client.transport.TransportClient;
import org.junit.rules.ExternalResource;

/**
 * A JUnit 4 {@link org.junit.rules.TestRule} for starting an elasticsearch server on the local machine.
 */
public class ElasticsearchRule extends ExternalResource {

    private final ElasticsearchBase base;

    public ElasticsearchRule(String clusterName) {
        base = new ElasticsearchBase(clusterName);
    }

    @Override
    protected void before() throws Exception {
        base.setup();
    }

    @Override
    protected void after() {
        base.teardown();
    }

    public TransportClient getTransportClient() {
        return base.getTransportClient();
    }

    public String getAddress() {
        return base.getAddress();
    }

    public void waitForGreenStatus() {
        base.waitForGreenStatus();
    }

}
