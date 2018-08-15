package com.boclips.mysql2es

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.data.annotation.Id
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service

data class Video(
        @Id val id: String? = null,
        val source: String? = null,
        val unique_id: String? = null,
        val namespace: String? = null,
        val title: String? = null,
        val description: String? = null,
        val date: String? = null,
        val duration: String? = null,
        val keywords: String? = null,
        val price_category: String? = null,
        val sounds: String? = null,
        val color: String? = null,
        val location: String? = null,
        val country: String? = null,
        val state: String? = null,
        val city: String? = null,
        val region: String? = null,
        val alternative_id: String? = null,
        val alt_source: String? = null,
        val restrictions: String? = null,
        val type_id: String? = null,
        val reference_id: String? = null
)

@Component
@ConfigurationProperties(prefix = "elasticsearch")
data class ElasticSearchProperties(
        var host: String = "",
        var port: Int = 0,
        var username: String = "",
        var password: String = ""
)

@Service
class EsClient(restTemplateBuilder: RestTemplateBuilder, elasticSearchProperties: ElasticSearchProperties) {
    private val restTemplate = restTemplateBuilder.rootUri("https://${elasticSearchProperties.host}:${elasticSearchProperties.port}").basicAuthorization(elasticSearchProperties.username, elasticSearchProperties.password).build()
    private var totalSent = 0

    fun index(videos: List<Video>, indexName: String) {
        val body = StringBuilder()

        videos.forEach { video ->
            val objectMapper = ObjectMapper()
            val document = objectMapper.writeValueAsString(video)

            body.appendln("{ \"index\" : { \"_index\" : \"$indexName\", \"_type\" : \"_doc\", \"_id\" : \"${video.id}\" } }")
            body.appendln(document)
        }

        print("Sending ${videos.size} videos to ES... ")

        val request = HttpEntity(body.toString(), HttpHeaders().apply { set("Content-Type", "application/x-ndjson") })
        val responseEntity = restTemplate.exchange("/$indexName/_bulk", HttpMethod.POST, request, String::class.java)

        totalSent += videos.size

        println("${responseEntity.statusCode.reasonPhrase}: $totalSent videos sent so far.")
    }
}


class Indexer(private val esClient: EsClient, private val indexName: String) {
    private val buffer: MutableList<Video> = mutableListOf()

    fun index(video: Video) {
        buffer.add(video)
        tryFlush()
    }

    fun flush() {
        esClient.index(buffer, indexName)
        buffer.clear()
    }

    private fun tryFlush() {
        if (buffer.size > 100) {
            this.flush()
        }
    }
}


@Service
class MigrationService(private val jdbcTemplate: JdbcTemplate, private val esClient: EsClient) {

    fun migrateData(query: String, indexName: String) {

        println(query)

        jdbcTemplate.query(query) { resultSet ->

            println("Indexing...")

            val indexer = Indexer(esClient, indexName)

            while (resultSet.next()) {
                val id = resultSet.getString("id")
                val source = resultSet.getString("source")
                val unique_id = resultSet.getString("unique_id")
                val namespace = resultSet.getString("namespace")
                val title = resultSet.getString("title")
                val description = resultSet.getString("description")
                val date = resultSet.getString("date")
                val duration = resultSet.getString("duration")
                val keywords = resultSet.getString("keywords")
                val price_category = resultSet.getString("price_category")
                val sounds = resultSet.getString("sounds")
                val color = resultSet.getString("color")
                val location = resultSet.getString("location")
                val country = resultSet.getString("country")
                val state = resultSet.getString("state")
                val city = resultSet.getString("city")
                val region = resultSet.getString("region")
                val alternative_id = resultSet.getString("alternative_id")
                val alt_source = resultSet.getString("alt_source")
                val restrictions = resultSet.getString("restrictions")
                val type_id = resultSet.getString("type_id")
                val reference_id = resultSet.getString("reference_id")

                indexer.index(Video(
                        id = id,
                        source = source,
                        unique_id = unique_id,
                        namespace = namespace,
                        title = title,
                        description = description,
                        date = date,
                        duration = duration,
                        keywords = keywords,
                        price_category = price_category,
                        sounds = sounds,
                        color = color,
                        location = location,
                        country = country,
                        state = state,
                        city = city,
                        region = region,
                        alternative_id = alternative_id,
                        alt_source = alt_source,
                        restrictions = restrictions,
                        type_id = type_id,
                        reference_id = reference_id))
            }

            indexer.flush()
        }
    }

}