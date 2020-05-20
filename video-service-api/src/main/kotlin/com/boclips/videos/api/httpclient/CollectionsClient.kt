package com.boclips.videos.api.httpclient

import com.boclips.videos.api.httpclient.helper.ObjectMapperDefinition
import com.boclips.videos.api.httpclient.helper.TokenFactory
import com.boclips.videos.api.request.Projection
import com.boclips.videos.api.request.collection.CollectionFilterRequest
import com.boclips.videos.api.request.collection.CreateCollectionRequest
import com.boclips.videos.api.request.collection.UpdateCollectionRequest
import com.boclips.videos.api.response.collection.CollectionResource
import com.boclips.videos.api.response.collection.CollectionsResource
import com.fasterxml.jackson.databind.ObjectMapper
import feign.Feign
import feign.Logger
import feign.Param
import feign.QueryMap
import feign.RequestLine
import feign.RequestTemplate
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import feign.okhttp.OkHttpClient
import feign.slf4j.Slf4jLogger

interface CollectionsClient {
    @RequestLine("GET /v1/collections")
    fun getCollections(@QueryMap collectionFilterRequest: CollectionFilterRequest = CollectionFilterRequest()): CollectionsResource

    @RequestLine("GET /v1/collections/{collectionId}?projection={projection}")
    fun getCollection(
        @Param("collectionId") collectionId: String,
        @Param("projection") projection: Projection? = null
    ): CollectionResource

    @RequestLine("POST /v1/collections")
    fun create(createCollectionRequest: CreateCollectionRequest): CollectionResource

    @RequestLine("PATCH /v1/collections/{collectionId}")
    fun update(
        @Param("collectionId") collectionId: String,
        update: UpdateCollectionRequest
    ): Void

    @RequestLine("DELETE /v1/collections/{collectionId}")
    fun delete(@Param("collectionId") collectionId: String)

    companion object {
        @JvmStatic
        fun create(
            apiUrl: String,
            objectMapper: ObjectMapper = ObjectMapperDefinition.default(),
            tokenFactory: TokenFactory? = null
        ): CollectionsClient {
            return Feign.builder()
                .client(OkHttpClient())
                .encoder(JacksonEncoder(objectMapper))
                .decoder(JacksonDecoder(objectMapper))
                .requestInterceptor { template: RequestTemplate ->
                    if (tokenFactory != null) {
                        template.header("Authorization", "Bearer ${tokenFactory.getAccessToken()}")
                    }
                }
                .logLevel(Logger.Level.BASIC)
                .logger(Slf4jLogger())
                .target(CollectionsClient::class.java, apiUrl)
        }
    }
}
