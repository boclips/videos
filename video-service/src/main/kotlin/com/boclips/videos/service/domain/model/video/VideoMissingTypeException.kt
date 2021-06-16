package com.boclips.videos.service.domain.model.video

class VideoMissingTypeException(videoId: VideoId) : Exception("The video of ID [$videoId] is missing type")
