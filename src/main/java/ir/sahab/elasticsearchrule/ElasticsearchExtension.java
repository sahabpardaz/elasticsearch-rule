package ir.sahab.elasticsearchrule;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.*;

/**
 * A JUnit 5 Extension for starting an elasticsearch server instance on the local machine.
 * By default, lifecycle of this extension is {@link org.junit.jupiter.api.TestInstance.Lifecycle#PER_CLASS}
 * you can specify it with this constructor
 * {@link ElasticsearchExtension#ElasticsearchExtension(String, TestInstance.Lifecycle)}
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
            setup();
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
            setup();
        }
    }
}
