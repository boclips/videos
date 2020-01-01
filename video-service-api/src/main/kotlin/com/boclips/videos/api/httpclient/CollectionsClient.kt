package com.boclips.videos.api.httpclient

import com.boclips.videos.api.response.collection.CollectionResource
import feign.Headers
import feign.Param
import feign.RequestLine
import org.springframework.hateoas.Resource

interface CollectionsClient : HttpClient {
    @RequestLine("GET /v1/collections/{collectionId}")
    @Headers("Authorization: Bearer token")
    fun collection(@Param("collectionId") collectionId: String): Resource<CollectionResource>
}