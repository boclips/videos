package com.boclips.videos.service.application.video.exceptions

import com.boclips.videos.service.domain.model.video.VideoId
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.NOT_FOUND)
class VideoTranscriptNotFound(val videoId: VideoId) :
    VideoServiceException("Video transcript for video '${videoId.value}' not found.")