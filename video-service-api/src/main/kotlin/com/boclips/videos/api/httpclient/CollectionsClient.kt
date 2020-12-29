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
import feign.Client
import feign.Param
import feign.QueryMap
import feign.RequestLine

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
            tokenFactory: TokenFactory? = null,
            feignClient: Client
        ) = FeignInterserviceClientFactory.create(
                apiUrl,
                objectMapper,
                tokenFactory,
                feignClient,
                CollectionsClient::class.java
        )
    }
}
