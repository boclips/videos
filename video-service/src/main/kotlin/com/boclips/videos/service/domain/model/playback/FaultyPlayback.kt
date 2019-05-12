package com.boclips.videos.service.domain.model.playback

import java.time.Duration

class FaultyPlayback(
    id: PlaybackId
) : VideoPlayback(id, "", Duration.ZERO)