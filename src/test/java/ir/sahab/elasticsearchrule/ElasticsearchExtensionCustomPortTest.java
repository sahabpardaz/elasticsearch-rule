package ir.sahab.elasticsearchrule;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.InfoResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;

import static ir.sahab.elasticsearchrule.ElasticsearchBase.anOpenPort;
import static org.junit.jupiter.api.Assertions.assertEquals;


class ElasticsearchExtensionCustomPortTest {
    private static final int customPort = anOpenPort();
    @RegisterExtension
    static ElasticsearchExtension elasticsearchExtension = new ElasticsearchExtension(customPort);
    static ElasticsearchClient elasticsearchClient;

    @BeforeAll
    static void setUpClass() {
        elasticsearchClient = elasticsearchExtension.getElasticsearchClient();
    }

    @Test
    void testPort() throws IOException {
        int elasticsearchPort = elasticsearchExtension.getPort();
        assertEquals(customPort, elasticsearchPort);

        InfoResponse response = elasticsearchClient.info();

        assertEquals("7.17.5", response.version().number());
    }

}
