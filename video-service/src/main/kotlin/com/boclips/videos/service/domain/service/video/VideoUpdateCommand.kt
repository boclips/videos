package com.boclips.videos.service.domain.service.video

import com.boclips.videos.service.domain.model.ageRange.AgeRange
import com.boclips.videos.service.domain.model.contentPartner.ContentPartner
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.domain.model.video.LegacySubject
import com.boclips.videos.service.domain.model.video.Topic
import com.boclips.videos.service.domain.model.video.VideoId
import java.time.Duration
import java.util.Locale

sealed class VideoUpdateCommand(val videoId: VideoId) {
    class ReplaceSubjects(videoId: VideoId, val subjects: List<LegacySubject>) : VideoUpdateCommand(videoId)
    class ReplaceDuration(videoId: VideoId, val duration: Duration) : VideoUpdateCommand(videoId)
    class ReplacePlayback(videoId: VideoId, val playback: VideoPlayback) : VideoUpdateCommand(videoId)
    class MakeSearchable(videoId: VideoId) : VideoUpdateCommand(videoId)
    class HideFromSearch(videoId: VideoId) : VideoUpdateCommand(videoId)
    class ReplaceLanguage(videoId: VideoId, val language: Locale) : VideoUpdateCommand(videoId)
    class ReplaceTranscript(videoId: VideoId, val transcript: String) : VideoUpdateCommand(videoId)
    class ReplaceTopics(videoId: VideoId, val topics: Set<Topic>) : VideoUpdateCommand(videoId)
    class ReplaceKeywords(videoId: VideoId, val keywords: Set<String>) : VideoUpdateCommand(videoId)
    class ReplaceAgeRange(videoId: VideoId, val ageRange: AgeRange) : VideoUpdateCommand(videoId)
    class ReplaceContentPartner(videoId: VideoId, val contentPartner: ContentPartner) : VideoUpdateCommand(videoId)
}

