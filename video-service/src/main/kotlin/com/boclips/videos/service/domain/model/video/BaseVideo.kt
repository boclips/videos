package com.boclips.videos.service.domain.model.video

import com.boclips.videos.service.domain.model.AgeRange
import com.boclips.videos.service.domain.model.attachment.Attachment
import com.boclips.videos.service.domain.model.contentwarning.ContentWarning
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.domain.model.tag.UserTag
import com.boclips.videos.service.domain.model.taxonomy.CategorySource
import com.boclips.videos.service.domain.model.taxonomy.CategoryWithAncestors
import com.boclips.videos.service.domain.model.user.UserId
import com.boclips.videos.service.domain.model.video.channel.Channel
import java.time.LocalDate
import java.time.ZonedDateTime

interface BaseVideo {
    val videoId: VideoId
    val playback: VideoPlayback
    val channel: Channel
    val videoReference: String
    val title: String
    val description: String
    val additionalDescription: String?
    val keywords: List<String>
    val releasedOn: LocalDate
    val ingestedAt: ZonedDateTime
    val types: List<VideoType>
    val legalRestrictions: String
    val subjects: VideoSubjects
    val topics: Set<Topic>
    val voice: Voice
    val ageRange: AgeRange
    val ratings: List<UserRating>
    val tags: List<UserTag>
    val promoted: Boolean?
    val attachments: List<Attachment>
    val contentWarnings: List<ContentWarning>?
    val deactivated: Boolean
    val activeVideoId: VideoId?
    val categories: Map<CategorySource, Set<CategoryWithAncestors>>

    fun isPlayable(): Boolean

    fun isBoclipsHosted(): Boolean

    fun getRatingAverage(): Double?

    fun isRatedByUser(user: UserId): Boolean

    fun hasTranscript(): Boolean

    fun isVoiced(): Boolean?
}
