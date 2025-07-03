package me.teixayo.bytetalk.backend.database.elastic;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;

public class ElasticDBConnection {

    private static ElasticDBConnection instance;

    private final ElasticsearchClient elasticClient;


    public ElasticDBConnection() {
        instance = this;
        RestClient restClient = RestClient.builder(new HttpHost("localhost", 9200, "http")).build();
        RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        elasticClient = new ElasticsearchClient(transport);
    }

}
