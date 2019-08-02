package com.boclips.videos.service.domain.service.video

import com.boclips.eventbus.EventBus
import com.boclips.eventbus.events.video.VideoCreated
import com.boclips.eventbus.events.video.VideoUpdated
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.EventConverter

class EventPublishingVideoRepository(private val videoRepository: VideoRepository, private val eventBus: EventBus) :
    VideoRepository by videoRepository {

    override fun  update(command: VideoUpdateCommand): Video {
        val video = videoRepository.update(command)

        publishVideoUpdated(video)

        return video
    }

    override fun bulkUpdate(commands: List<VideoUpdateCommand>): List<Video> {
        val videos = videoRepository.bulkUpdate(commands)

        videos.forEach(this::publishVideoUpdated)

        return videos
    }

    override fun create(video: Video): Video {
        val videoCreated = videoRepository.create(video)

        publishVideoCreated(videoCreated)

        return videoCreated
    }

    private fun publishVideoUpdated(video: Video) {
        eventBus.publish(VideoUpdated.of(EventConverter().toVideoPayload(video)))
    }

    private fun publishVideoCreated(video: Video) {
        eventBus.publish(
            VideoCreated.builder()
                .video(EventConverter().toVideoPayload(video))
                .build()
        )
    }
}
