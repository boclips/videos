package com.boclips.search.service.infrastructure

import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.impl.client.BasicCredentialsProvider
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient

data class ElasticSearchClient(
    val scheme: String,
    val host: String,
    val port: Int,
    val username: String,
    val password: String
) {
    fun buildClient(): RestHighLevelClient {
        val credentialsProvider = BasicCredentialsProvider()
        credentialsProvider.setCredentials(AuthScope.ANY, UsernamePasswordCredentials(this.username, this.password))

        val builder = RestClient.builder(HttpHost(this.host, this.port, this.scheme))
            .setHttpClientConfigCallback { httpClientBuilder ->
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
            }
        return RestHighLevelClient(builder)
    }
}
