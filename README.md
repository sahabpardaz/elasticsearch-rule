# Elasticsearch Rule
A JUnit rule for starting an elasticsearch server on the local machine.

## Sample Usage

```java
@ClassRule
public static final ElasticsearchRule elasticsearchRule = new ElasticsearchRule();

private static RestHighLevelClient restHighLevelClient;

@BeforeClass
public static void setUpClass() {
    restHighLevelClient = elasticsearchRule.getRestHighLevelClient();
}

@Test
public void test() {
    String indexName = "twitter";
    CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName);
    CreateIndexResponse createIndexResponse = restHighLevelClient.indices()
            .create(createIndexRequest,RequestOptions.DEFAULT);
    Assert.assertTrue(createIndexResponse.isAcknowledged());
}
```
It is also possible to get the network address of the Elasticsearch server and construct the RestHighLevelClient:
```java
@BeforeClass
public static void setUpClass() {
    String address = elasticsearchRule.getAddress();
    String elasticsearchHost = address.split(":")[0];
    int elasticsearchPort = Integer.parseInt(address.split(":")[1]);
    RestHighLevelClient anotherRestHighLevelClient = new RestHighLevelClient(RestClient.builder(
            new HttpHost(elasticsearchHost, elasticsearchPort, "http")));
}
```

## Add it to your project
You can refer to this library by either of java build systems (Maven, Gradle, SBT or Leiningen) using snippets from this jitpack link:
[![](https://jitpack.io/v/sahabpardaz/elasticsearch-rule.svg)](https://jitpack.io/#sahabpardaz/elasticsearch-rule)
