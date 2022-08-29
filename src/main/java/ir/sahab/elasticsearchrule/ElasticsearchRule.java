package ir.sahab.elasticsearchrule;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.MultipleFailureException;
import org.junit.runners.model.Statement;

import java.util.ArrayList;
import java.util.List;

/**
 * A JUnit 4 {@link org.junit.rules.TestRule} for starting an elasticsearch server on the local machine.
 */
public class ElasticsearchRule extends ElasticsearchBase implements TestRule {

    public ElasticsearchRule(String clusterName) {
        super(clusterName);
    }

    public Statement apply(Statement base, Description description) {
        return statement(base);
    }

    // copied from org.junit.rules.ExternalResource
    private Statement statement(final Statement base) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                setup();

                List<Throwable> errors = new ArrayList<>();
                try {
                    base.evaluate();
                } catch (Throwable t) {
                    errors.add(t);
                } finally {
                    try {
                        teardown();
                    } catch (Throwable t) {
                        errors.add(t);
                    }
                }
                MultipleFailureException.assertEmpty(errors);
            }
        };
    }
}
