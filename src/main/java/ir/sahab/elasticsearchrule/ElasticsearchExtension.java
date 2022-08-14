package ir.sahab.elasticsearchrule;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.*;

/**
 * A JUnit 5 Extension for starting an elasticsearch server instance on the local machine.
 */
public class ElasticsearchExtension extends ElasticsearchBase
        implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback {

    private final TestInstance.Lifecycle lifecycle;

    public ElasticsearchExtension(String clusterName) {
        super(clusterName);
        lifecycle = TestInstance.Lifecycle.PER_CLASS;
    }

    public ElasticsearchExtension(String clusterName, TestInstance.Lifecycle lifecycle) {
        super(clusterName);
        this.lifecycle = lifecycle;
    }

    @Override
    public void afterAll(ExtensionContext context) {
        if (lifecycle == TestInstance.Lifecycle.PER_CLASS) {
            teardown();
        }
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        if (lifecycle == TestInstance.Lifecycle.PER_CLASS) {
            setupElasticsearchServer();
        }
    }

    @Override
    public void afterEach(ExtensionContext context) {
        if (lifecycle == TestInstance.Lifecycle.PER_METHOD) {
            teardown();
        }
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        if (lifecycle == TestInstance.Lifecycle.PER_METHOD) {
            setupElasticsearchServer();
        }
    }
}
