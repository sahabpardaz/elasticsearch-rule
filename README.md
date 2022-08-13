# Elasticsearch Rule

[![Tests](https://github.com/sahabpardaz/elasticsearch-rule/actions/workflows/maven.yml/badge.svg?branch=master)](https://github.com/sahabpardaz/elasticsearch-rule/actions/workflows/maven.yml)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=sahabpardaz_elasticsearch-rule&metric=coverage)](https://sonarcloud.io/dashboard?id=sahabpardaz_elasticsearch-rule)
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=sahabpardaz_elasticsearch-rule&metric=duplicated_lines_density)](https://sonarcloud.io/dashboard?id=sahabpardaz_elasticsearch-rule)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=sahabpardaz_elasticsearch-rule&metric=vulnerabilities)](https://sonarcloud.io/dashboard?id=sahabpardaz_elasticsearch-rule)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=sahabpardaz_elasticsearch-rule&metric=security_rating)](https://sonarcloud.io/dashboard?id=sahabpardaz_elasticsearch-rule)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=sahabpardaz_elasticsearch-rule&metric=reliability_rating)](https://sonarcloud.io/dashboard?id=sahabpardaz_elasticsearch-rule)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=sahabpardaz_elasticsearch-rule&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=sahabpardaz_elasticsearch-rule)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=sahabpardaz_elasticsearch-rule&metric=sqale_index)](https://sonarcloud.io/dashboard?id=sahabpardaz_elasticsearch-rule)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=sahabpardaz_elasticsearch-rule&metric=alert_status)](https://sonarcloud.io/dashboard?id=sahabpardaz_elasticsearch-rule)

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

## JUnit 5

Sample usage:

```java
private static final String ELASTICSEARCH_CLUSTER_NAME="elasticsearch";

@RegisterExtension
static ElasticsearchExtension elasticsearchExtension=new ElasticsearchExtension(ELASTICSEARCH_CLUSTER_NAME);
private static TransportClient transportClient;

@BeforeAll
static void setUpClass(){
        transportClient=elasticsearchExtension.getTransportClient();
}

@Test
public void testClient(){
        String indexName="twitter";
        CreateIndexResponse createIndexResponse=transportClient.admin().indices().prepareCreate(indexName).get();
        Assert.assertTrue(createIndexResponse.isAcknowledged());
}
```

## Add it to your project

You can refer to this library by either of java build systems (Maven, Gradle, SBT or Leiningen) using snippets from this
jitpack link:
[![](https://jitpack.io/v/sahabpardaz/elasticsearch-rule.svg)](https://jitpack.io/#sahabpardaz/elasticsearch-rule)

JUnit 4 and 5 dependencies are marked as optional, so you need to provide JUnit 4 or 5 dependency
(based on what version you need, and you use) in you project to make it work.
