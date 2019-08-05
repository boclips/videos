package com.boclips.videos.service.domain.service.video

import com.boclips.videos.service.domain.model.video.Video

class VideoNotCreatedException(val video: Video) : RuntimeException()
