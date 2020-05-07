package com.boclips.videos.service.domain.model.video

import com.boclips.videos.service.domain.model.playback.PlaybackId
import java.lang.RuntimeException

class UnknownCaptionFormatException(playbackId: PlaybackId, format: Any):
    RuntimeException("Unknown caption format '$format' for playback ID: $playbackId") {
}