package com.boclips.videos.service.domain.service

import com.boclips.videos.service.domain.model.Video

interface EventService {
    fun analyseVideo(video: Video)
}

