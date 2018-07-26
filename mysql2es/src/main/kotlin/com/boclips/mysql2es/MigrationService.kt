package com.boclips.mysql2es

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.data.annotation.Id
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.RequestEntity
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange
import org.springframework.web.client.postForEntity
import java.net.URI


data class Video(
        @Id
        public val id: String? = null,
        public val source: String? = null,
        public val unique_id: String? = null,
        public val namespace: String? = null,
        public val title: String? = null,
        public val description: String? = null,
        public val date: String? = null,
        public val duration: String? = null,
        public val keywords: String? = null,
        public val price_category: String? = null,
        public val sounds: String? = null,
        public val color: String? = null,
        public val location: String? = null,
        public val country: String? = null,
        public val state: String? = null,
        public val city: String? = null,
        public val region: String? = null,
        public val alternative_id: String? = null,
        public val alt_source: String? = null,
        public val restrictions: String? = null,
        public val type_id: String? = null,
        public val reference_id: String? = null
)

@Service
class EsClient(private val restTemplateBuilder: RestTemplateBuilder) {
    private val restTemplate = restTemplateBuilder.rootUri("https://search-test-search-gvzmbjdq7khuhjdtb33zbldpqu.eu-west-1.es.amazonaws.com").build()
    private var totalSent = 0

    fun index(videos: List<Video>) {
        val body = StringBuilder()

        videos.forEach { video ->
            val objectMapper = ObjectMapper()
            val document = objectMapper.writeValueAsString(video)

            body.appendln("{ \"index\" : { \"_index\" : \"curated-videos\", \"_type\" : \"_doc\", \"_id\" : \"${video.id}\" } }")
            body.appendln(document)
        }

        println("Sending ${videos.size} videos to ES")

        val request = HttpEntity(body.toString(), HttpHeaders().apply { set("Content-Type", "application/x-ndjson") })
        val responseEntity = restTemplate.exchange("/curated-videos/_bulk", HttpMethod.POST, request, String::class.java)

        totalSent += videos.size

        println("Response was ${responseEntity.statusCode}: $totalSent videos sent so far")
    }
}


@Service
class Indexer(private val esClient: EsClient) {
    private val buffer: MutableList<Video> = mutableListOf()

    fun index(video: Video) {
        buffer.add(video)
        tryFlush()
    }

    fun flush() {
        esClient.index(buffer)
        buffer.clear()
    }

    private fun tryFlush() {
        if (buffer.size > 1000) {
            this.flush()
        }
    }
}


@Service
class MigrationService(private val jdbcTemplate: JdbcTemplate, private val indexer: Indexer) {

    fun migrateData(query: String) {
        jdbcTemplate.query(query) { resultSet ->

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