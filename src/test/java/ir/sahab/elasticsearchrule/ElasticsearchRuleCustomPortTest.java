package ir.sahab.elasticsearchrule;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.InfoResponse;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;

import static ir.sahab.elasticsearchrule.ElasticsearchRule.anOpenPort;
import static org.junit.Assert.assertEquals;

public class ElasticsearchRuleCustomPortTest {

    private static final int customPort = anOpenPort();

    @ClassRule
    public static final ElasticsearchRule elasticsearchRule = new ElasticsearchRule(customPort);

    private static ElasticsearchClient elasticsearchClient;

    @BeforeClass
    public static void setUpClass() {
        elasticsearchClient = elasticsearchRule.getElasticsearchClient();
    }

    @Test
    public void testPort() throws IOException {
        int elasticsearchPort = elasticsearchRule.getPort();
        assertEquals(customPort, elasticsearchPort);

        InfoResponse response = elasticsearchClient.info();

        assertEquals("7.17.5", response.version().number());
    }
}
