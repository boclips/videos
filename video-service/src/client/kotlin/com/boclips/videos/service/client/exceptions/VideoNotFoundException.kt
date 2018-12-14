package com.boclips.videos.service.client.exceptions

import com.boclips.videos.service.client.VideoId

class VideoNotFoundException(videoId: VideoId) : Exception(videoId.uri.toString())