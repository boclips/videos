package com.boclips.videos.api.httpclient.test.fakes

import com.boclips.videos.api.httpclient.VideosClient
import com.boclips.videos.api.request.video.CreateVideoRequest
import com.boclips.videos.api.request.video.SearchVideosRequest
import com.boclips.videos.api.request.video.UpdateVideoRequest
import com.boclips.videos.api.response.subject.SubjectResource
import com.boclips.videos.api.response.video.VideoResource
import com.boclips.videos.api.response.video.VideosResource
import com.boclips.videos.api.response.video.VideosWrapperResource
import feign.FeignException
import org.springframework.hateoas.PagedResources
import kotlin.math.ceil

class VideosClientFake : VideosClient, FakeClient<VideoResource> {
    private val database: MutableMap<String, VideoResource> = LinkedHashMap()
    private var id = 0

    override fun getVideo(
        videoId: String
    ): VideoResource {
        return database[videoId]!!
    }

    override fun probeVideoReference(contentPartnerId: String, contentPartnerVideoId: String) {
        val results = database
            .filter { it.value.contentPartnerId == contentPartnerId && it.value.contentPartnerVideoId == contentPartnerVideoId }

        if (results.isEmpty()) throw FeignException.FeignClientException(404, "resource not found", null, null)
    }

    override fun searchVideos(searchVideosRequest: SearchVideosRequest): VideosResource {
        val pageSize = searchVideosRequest.size ?: 100
        val pageNumber = searchVideosRequest.page ?: 0
        return VideosResource(
            _embedded = VideosWrapperResource(
                database.values.toList().drop(pageNumber * pageSize).take((pageNumber + 1) * pageSize)
            ),
            page = PagedResources.PageMetadata(
                pageSize.toLong(),
                pageNumber.toLong(),
                database.values.size.toLong(),
                ceil(database.values.size.toDouble() / pageSize).toLong()
            )
        )
    }

    override fun updateVideo(videoId: String, updateVideoRequest: UpdateVideoRequest) {
        val video = database[videoId]!!

        val updatedVideo = video.copy(
            description = updateVideoRequest.description ?: video.description,
            title = updateVideoRequest.title ?: video.title,
            promoted = updateVideoRequest.promoted ?: video.promoted,
            subjects = updateVideoRequest.subjectIds?.let { subjects ->
                subjects.map { SubjectResource(id = it) }.toSet()
            } ?: video.subjects
        )

        database[videoId] = updatedVideo
    }

    override fun createVideo(createVideoRequest: CreateVideoRequest): VideoResource {
        val newVideo = VideoResource(
            id = "${id++}",
            title = createVideoRequest.title,
            description = createVideoRequest.description,
            _links = null
        )

        database[newVideo.id!!] = newVideo
        return newVideo
    }

    override fun deleteVideo(videoId: String) {
        database.remove(videoId)
    }

    override fun updateVideoRating(videoId: String, rating: Int) {
        val updatedVideo = database[videoId]!!.copy(yourRating = rating.toDouble())
        database.replace(videoId, updatedVideo)
    }

    override fun updateVideoSharing(videoId: String, sharing: Boolean) {
        TODO("not implemented")
    }

    override fun getVideoTranscript(videoId: String): String {
        TODO("not implemented")
    }

    override fun add(element: VideoResource): VideoResource {
        database[element.id!!] = element
        return element
    }

    override fun clear() {
        database.clear()
    }

    override fun findAll(): List<VideoResource> {
        return database.values.toList()
    }
}
