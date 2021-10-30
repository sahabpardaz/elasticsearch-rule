# Elasticsearch Rule
[![Tests](https://github.com/sahabpardaz/elasticsearch-rule/actions/workflows/maven.yml/badge.svg?branch=es-7)](https://github.com/sahabpardaz/elasticsearch-rule/actions/workflows/maven.yml)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=sahabpardaz_elasticsearch-rule&metric=coverage&branch=es-7)](https://sonarcloud.io/dashboard?id=sahabpardaz_elasticsearch-rule)
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=sahabpardaz_elasticsearch-rule&metric=duplicated_lines_density&branch=es-7)](https://sonarcloud.io/dashboard?id=sahabpardaz_elasticsearch-rule)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=sahabpardaz_elasticsearch-rule&metric=vulnerabilities&branch=es-7)](https://sonarcloud.io/dashboard?id=sahabpardaz_elasticsearch-rule)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=sahabpardaz_elasticsearch-rule&metric=security_rating&branch=es-7)](https://sonarcloud.io/dashboard?id=sahabpardaz_elasticsearch-rule)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=sahabpardaz_elasticsearch-rule&metric=reliability_rating&branch=es-7)](https://sonarcloud.io/dashboard?id=sahabpardaz_elasticsearch-rule)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=sahabpardaz_elasticsearch-rule&metric=sqale_rating&branch=es-7)](https://sonarcloud.io/dashboard?id=sahabpardaz_elasticsearch-rule)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=sahabpardaz_elasticsearch-rule&metric=sqale_index&branch=es-7)](https://sonarcloud.io/dashboard?id=sahabpardaz_elasticsearch-rule)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=sahabpardaz_elasticsearch-rule&metric=alert_status&branch=es-7)](https://sonarcloud.io/dashboard?id=sahabpardaz_elasticsearch-rule)

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
    String elasticsearchHost = elasticsearchRule.getHost();
    int elasticsearchPort = elasticsearchRule.getPort();
    RestHighLevelClient anotherRestHighLevelClient = new RestHighLevelClient(RestClient.builder(
            new HttpHost(elasticsearchHost, elasticsearchPort, "http")));
}
```

## Add it to your project
You can refer to this library by either of java build systems (Maven, Gradle, SBT or Leiningen) using snippets from this jitpack link:
[![](https://jitpack.io/v/sahabpardaz/elasticsearch-rule.svg)](https://jitpack.io/#sahabpardaz/elasticsearch-rule)
