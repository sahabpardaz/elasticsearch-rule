package ir.sahab.elasticsearchrule;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.*;

/**
 * A JUnit 5 Extension for starting an elasticsearch server instance on the local machine.
 */
public class ElasticsearchExtension extends ElasticsearchBase
        implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback {

    private final TestInstance.Lifecycle lifecycle;

    public ElasticsearchExtension(int port, TestInstance.Lifecycle lifecycle) {
        super(port);
        this.lifecycle = lifecycle;
    }

    public ElasticsearchExtension() {
        super();
        lifecycle = TestInstance.Lifecycle.PER_CLASS;
    }

    public ElasticsearchExtension(int port) {
        super(port);
        lifecycle = TestInstance.Lifecycle.PER_CLASS;
    }

    @Override
    public void afterAll(ExtensionContext context) {
        if (lifecycle == TestInstance.Lifecycle.PER_CLASS) {
            teardown();
        }
    }

    @Override
    public void afterEach(ExtensionContext context) {
        if (lifecycle == TestInstance.Lifecycle.PER_METHOD) {
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
    public void beforeEach(ExtensionContext context) throws Exception {
        if (lifecycle == TestInstance.Lifecycle.PER_METHOD) {
            setup();
        }

    }
}
