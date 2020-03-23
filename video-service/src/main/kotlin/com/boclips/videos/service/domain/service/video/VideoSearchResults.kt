package com.boclips.videos.service.domain.service.video

import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.VideoCounts

class VideoSearchResults(val counts: VideoCounts, val videos: List<Video>)
