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
public static final ElasticsearchRule elasticsearchRule=new ElasticsearchRule();

private static ElasticsearchClient elasticsearchClient;

@BeforeClass
public static void setUpClass(){
        elasticsearchClient=elasticsearchRule.getElasticsearchClient();
        }

@Test
public void test(){
        String indexName="twitter";
        CreateIndexRequest createIndexRequest=new CreateIndexRequest.Builder()
        .index(indexName)
        .build();
        CreateIndexResponse createIndexResponse=elasticsearchClient.indices().create(createIndexRequest);
        Assert.assertTrue(createIndexResponse.acknowledged());
        }
```

It is also possible to get the network address of the Elasticsearch server and construct the ElasticsearchClient:

```java
private static ElasticsearchClient anotherElasticsearchClient;

@BeforeClass
public static void setUpClass(){
        String elasticsearchHost=elasticsearchRule.getHost();
        int elasticsearchPort=elasticsearchRule.getPort();
final RestClient restClient=RestClient.builder(new HttpHost(elasticsearchHost,elasticsearchPort,"http")).build();
final RestClientTransport transport=new RestClientTransport(restClient,new JacksonJsonpMapper())
        anotherElasticsearchClient=new ElasticsearchClient(transport);
        }
```

## JUnit 5 Support

Sample usage:

```java
@RegisterExtension
static ElasticsearchExtension elasticsearchExtension=new ElasticsearchExtension();
static ElasticsearchClient elasticsearchClient;

@BeforeAll
static void setUpClass(){
        elasticsearchClient=elasticsearchExtension.getElasticsearchClient();
        }

@Test
void testClient()throws IOException{
        String indexName="twitter";
        CreateIndexRequest createIndexRequest=new CreateIndexRequest.Builder()
        .index(indexName)
        .build();
        CreateIndexResponse createIndexResponse=elasticsearchClient.indices().create(createIndexRequest);
        assertTrue(createIndexResponse.acknowledged());
        }
```

## Add it to your project

You can refer to this library by either of java build systems (Maven, Gradle, SBT or Leiningen) using snippets from this
jitpack link:
[![](https://jitpack.io/v/sahabpardaz/elasticsearch-rule.svg)](https://jitpack.io/#sahabpardaz/elasticsearch-rule)

JUnit 4 and 5 dependencies are marked as optional, so you need to provide JUnit 4 or 5 dependency (based on what version
you need, and you use) in you project to make it work.
