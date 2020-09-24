package com.boclips.videos.api.httpclient.test.fakes

import com.boclips.videos.api.httpclient.VideosClient
import com.boclips.videos.api.request.Projection
import com.boclips.videos.api.request.video.CreateVideoRequest
import com.boclips.videos.api.request.video.SearchVideosRequest
import com.boclips.videos.api.request.video.StreamPlaybackResource
import com.boclips.videos.api.request.video.UpdateVideoRequest
import com.boclips.videos.api.request.video.YoutubePlaybackResource
import com.boclips.videos.api.response.agerange.AgeRangeResource
import com.boclips.videos.api.response.subject.SubjectResource
import com.boclips.videos.api.response.video.CaptionStatus
import com.boclips.videos.api.response.video.VideoResource
import com.boclips.videos.api.response.video.VideosResource
import com.boclips.videos.api.response.video.VideosWrapperResource
import org.springframework.hateoas.PagedModel
import java.time.LocalDate
import kotlin.math.ceil

class VideosClientFake : VideosClient, FakeClient<VideoResource> {
    private val database: MutableMap<String, VideoResource> = LinkedHashMap()
    private var id = 0

    override fun getVideo(
        videoId: String,
        projection: Projection
    ): VideoResource {
        return database[videoId] ?: throw FakeClient.notFoundException("Video not found")
    }

    override fun probeVideoReference(channelId: String, channelVideoId: String) {
        val results = database
            .filter { it.value.channelId == channelId && it.value.channelVideoId == channelVideoId }

        if (results.isEmpty()) throw FakeClient.notFoundException("Video not found")
    }

    override fun searchVideos(searchVideosRequest: SearchVideosRequest): VideosResource {
        val pageSize = searchVideosRequest.size ?: 100
        val pageNumber = searchVideosRequest.page ?: 0
        return VideosResource(
            _embedded = VideosWrapperResource(
                videos = database.values.toList().drop(pageNumber * pageSize).take((pageNumber + 1) * pageSize),
                facets = null
            ),
            page = PagedModel.PageMetadata(
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
            additionalDescription = createVideoRequest.additionalDescription,
            channelId = createVideoRequest.providerId,
            playback = when (createVideoRequest.playbackProvider) {
                "YOUTUBE" -> YoutubePlaybackResource(id = createVideoRequest.playbackId)
                "KALTURA" -> StreamPlaybackResource(
                    id = createVideoRequest.playbackId,
                    referenceId = createVideoRequest.playbackProvider,
                    duration = null
                )
                else -> throw IllegalStateException("Could not determine the playback provider ${createVideoRequest.playbackProvider}")
            },
            releasedOn = LocalDate.now(),
            legalRestrictions = createVideoRequest.legalRestrictions,
            ageRange = AgeRangeResource(min = createVideoRequest.ageRangeMin, max = createVideoRequest.ageRangeMax),
            channelVideoId = createVideoRequest.providerVideoId,
            isVoiced = createVideoRequest.isVoiced,
            _links = null,
            captionStatus = CaptionStatus.NOT_AVAILABLE
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

    override fun requestVideoCaptions(videoId: String) {
        if (database[videoId]?.captionStatus == CaptionStatus.REQUESTED ||
            database[videoId]?.captionStatus == CaptionStatus.PROCESSING ||
            database[videoId]?.captionStatus == CaptionStatus.HUMAN_GENERATED_AVAILABLE
        ) {
            throw FakeClient.conflictException("Captions exist or are already requested!")
        }

        database[videoId] = database[videoId]?.copy(captionStatus = CaptionStatus.REQUESTED)
            ?: VideoResource(id = videoId, captionStatus = CaptionStatus.REQUESTED, _links = emptyMap())
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
