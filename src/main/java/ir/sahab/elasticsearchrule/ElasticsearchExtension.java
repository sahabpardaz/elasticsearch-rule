package ir.sahab.elasticsearchrule;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * A JUnit 5 Extension for starting an elasticsearch server instance on the local machine.
 */
public class ElasticsearchExtension extends ElasticsearchBase implements BeforeAllCallback, AfterAllCallback {

    public ElasticsearchExtension(String clusterName) {
        super(clusterName);
    }

    @Override
    public void afterAll(ExtensionContext context) {
        teardown();
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        setupElasticsearchServer();
    }

}
