package com.boclips.videos.service.domain.service.video

import com.boclips.videos.service.domain.model.common.AgeRange
import com.boclips.contentpartner.service.domain.model.ContentPartner
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.domain.model.subject.Subject
import com.boclips.videos.service.domain.model.tag.UserTag
import com.boclips.videos.service.domain.model.video.DistributionMethod
import com.boclips.videos.service.domain.model.video.Topic
import com.boclips.videos.service.domain.model.video.UserRating
import com.boclips.videos.service.domain.model.video.VideoId
import java.time.Duration
import java.util.Locale

sealed class VideoUpdateCommand(val videoId: VideoId) {
    class ReplaceSubjects(videoId: VideoId, val subjects: List<Subject>) : VideoUpdateCommand(videoId)
    class ReplaceDuration(videoId: VideoId, val duration: Duration) : VideoUpdateCommand(videoId)
    class ReplacePlayback(videoId: VideoId, val playback: VideoPlayback) : VideoUpdateCommand(videoId)
    class ReplaceDistributionMethods(videoId: VideoId, val distributionMethods: Set<DistributionMethod>) :
        VideoUpdateCommand(videoId)

    class ReplaceLanguage(videoId: VideoId, val language: Locale) : VideoUpdateCommand(videoId)
    class ReplaceTranscript(videoId: VideoId, val transcript: String) : VideoUpdateCommand(videoId)
    class ReplaceTopics(videoId: VideoId, val eventBus: Set<Topic>) : VideoUpdateCommand(videoId)
    class ReplaceKeywords(videoId: VideoId, val keywords: Set<String>) : VideoUpdateCommand(videoId)
    class ReplaceAgeRange(videoId: VideoId, val ageRange: AgeRange) : VideoUpdateCommand(videoId)
    class ReplaceContentPartner(videoId: VideoId, val contentPartner: ContentPartner) : VideoUpdateCommand(videoId)
    class AddRating(videoId: VideoId, val rating: UserRating) : VideoUpdateCommand(videoId)
    class ReplaceTag(videoId: VideoId, val tag: UserTag) : VideoUpdateCommand(videoId)
    class ReplaceTitle(videoId: VideoId, val title: String) : VideoUpdateCommand(videoId)
    class ReplaceDescription(videoId: VideoId, val description: String) : VideoUpdateCommand(videoId)
}

