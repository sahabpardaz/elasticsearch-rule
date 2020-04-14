# Elasticsearch Rule
A JUnit rule for starting an elasticsearch server on the local machine.

## Sample Usage

```java
private static final String ELASTICSEARCH_CLUSTER_NAME = "elasticsearch";

@ClassRule
public static final ElasticsearchRule elasticsearchRule = new ElasticsearchRule(ELASTICSEARCH_CLUSTER_NAME);

private static TransportClient transportClient;

@BeforeClass
public static void setUpClass() {
    transportClient = elasticsearchRule.getTransportClient();
}

@Test
public void testClient() {
    String indexName = "twitter";
    CreateIndexResponse createIndexResponse = transportClient.admin().indices().prepareCreate(indexName).get();
    Assert.assertTrue(createIndexResponse.isAcknowledged());
}
``` 
It is also possible to get the network address of the Elasticsearch server and construct the TransportClient:
```java
@BeforeClass
public static void setUpClass() {
    String address = elasticsearchRule.getAddress();
    String elasticsearchHost = address.split(":")[0];
    int elasticsearchPort = Integer.parseInt(address.split(":")[1]);
    InetAddress elasticsearchInetAddress;
    try {
        elasticsearchInetAddress = InetAddress.getByName(elasticsearchHost);
    } catch (UnknownHostException e) {
        throw new AssertionError("Cannot get the elasticsearch server address " + elasticsearchHost + ".", e);
    }
    Settings settings = Settings.builder().put("cluster.name", ELASTICSEARCH_CLUSTER_NAME).build();
    transportClient = new PreBuiltTransportClient(settings);
    transportClient.addTransportAddress(new TransportAddress(elasticsearchInetAddress, elasticsearchPort));
}
```

## Add it to your project
You can refer to this library by either of java build systems (Maven, Gradle, SBT or Leiningen) using snippets from this jitpack link:
[![](https://jitpack.io/v/sahabpardaz/elasticsearch-rule.svg)](https://jitpack.io/#sahabpardaz/elasticsearch-rule)
