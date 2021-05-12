package com.boclips.videos.service.domain.service.video

import com.boclips.eventbus.EventBus
import com.boclips.eventbus.events.video.VideoCreated
import com.boclips.eventbus.events.video.VideoUpdated
import com.boclips.eventbus.events.video.VideosUpdated
import com.boclips.videos.service.config.properties.BatchProcessingConfig
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.VideoFilter
import com.boclips.videos.service.domain.service.events.EventConverter

class VideoRepositoryEventDecorator(
    private val videoRepository: VideoRepository,
    private val eventBus: EventBus,
    val batchProcessingConfig: BatchProcessingConfig
) :
    VideoRepository by videoRepository {

    override fun update(command: VideoUpdateCommand): Video {
        val video = videoRepository.update(command)

        publishVideoUpdated(video)

        return video
    }

    override fun bulkUpdate(commands: List<VideoUpdateCommand>): List<Video> {
        val videos = videoRepository.bulkUpdate(commands)

        publishVideosUpdated(videos)

        return videos
    }

    override fun create(video: Video): Video {
        val videoCreated = videoRepository.create(video)

        publishVideoCreated(videoCreated)

        return videoCreated
    }

    override fun streamUpdate(
        filter: VideoFilter,
        consumer: (List<Video>) -> List<VideoUpdateCommand>
    ): Sequence<Video> {
        val videos = videoRepository.streamUpdate(filter) { videos ->
            consumer(videos)
        }
        videos.windowed(
            size = batchProcessingConfig.videoBatchSize,
            step = batchProcessingConfig.videoBatchSize,
            partialWindows = true
        ).forEach { batch ->
            publishVideosUpdated(batch)
        }
        return videos
    }

    private fun publishVideoUpdated(video: Video) {
        eventBus.publish(
            VideoUpdated.of(
                EventConverter().toVideoPayload(video)
            )
        )
    }

    private fun publishVideosUpdated(videos: List<Video>) {
        eventBus.publish(
            VideosUpdated.builder().videos(
                videos.map {
                    EventConverter()
                        .toVideoPayload(it)
                }
            ).build()
        )
    }

    private fun publishVideoCreated(video: Video) {
        eventBus.publish(
            VideoCreated.builder()
                .video(EventConverter().toVideoPayload(video))
                .build()
        )
    }
}
