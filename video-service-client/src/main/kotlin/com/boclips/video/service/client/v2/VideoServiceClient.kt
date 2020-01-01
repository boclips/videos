package com.boclips.video.service.client.v2

import com.boclips.videos.api.request.video.AdminSearchRequest
import com.boclips.videos.api.request.video.CreateVideoRequest
import com.boclips.videos.api.response.collection.CollectionResource
import com.boclips.videos.api.response.subject.SubjectResource
import com.boclips.videos.api.response.video.VideoResource
import feign.Headers
import feign.Param
import feign.QueryMap
import feign.RequestLine
import org.springframework.hateoas.Resource
import org.springframework.hateoas.Resources

interface VideoServiceClient {
    /**
     * Video endpoints
     */
    @RequestLine("GET /v1/videos/{videoId}")
    fun getVideo(@Param("videoId") videoId: String): Resource<VideoResource>

    @RequestLine("GET /v1/videos/{videoId}/transcript")
    fun getVideoTranscript(@Param("videoId") videoId: String): String

    @RequestLine("DELETE /v1/videos/{videoId}")
    fun createVideo(@QueryMap createVideoRequest: CreateVideoRequest): Resource<VideoResource>

    @RequestLine("DELETE /v1/videos/{videoId}")
    fun deleteVideo(@Param("videoId") videoId: String): Resource<VideoResource>

    @RequestLine("PATCH /v1/videos/{videoId}")
    fun updateVideo(@Param("videoId") videoId: String): Resource<Void>

    @RequestLine("POST /v1/search")
    fun searchVideos(@QueryMap searchRequest: AdminSearchRequest): Resources<VideoResource>

    @RequestLine("PATCH /v1/videos/{videoId}?rating={rating}")
    fun updateVideoRating(@Param("videoId") videoId: String, @Param("rating") rating: String): Resource<Void>

    @RequestLine("PATCH /v1/videos/{videoId}?sharing=true")
    fun updateVideoSharing(@Param("videoId") videoId: String): Resource<Void>

    /**
     * Subject endpoints
     */
    @RequestLine("GET /v1/subjects")
    fun subjects(): Resources<SubjectResource>

    /**
     * Collection endpoints
     */
    @RequestLine("GET /v1/collections/{collectionId}")
    @Headers("Authorization: Bearer token")
    fun collection(@Param("collectionId") collectionId: String): Resource<CollectionResource>
}