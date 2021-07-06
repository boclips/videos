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

data class Video(
    override val videoId: VideoId,
    override val playback: VideoPlayback,
    override val channel: Channel,
    override val videoReference: String,
    override val title: String,
    override val description: String,
    override val additionalDescription: String?,
    override val keywords: List<String>,
    override val releasedOn: LocalDate,
    override val ingestedAt: ZonedDateTime,
    override val updatedAt: ZonedDateTime,
    override val types: List<VideoType>,
    override val legalRestrictions: String,
    override val subjects: VideoSubjects,
    override val topics: Set<Topic>,
    override val voice: Voice,
    override val ageRange: AgeRange,
    override val ratings: List<UserRating>,
    override val tags: List<UserTag>,
    override val promoted: Boolean?,
    override val attachments: List<Attachment>,
    override val contentWarnings: List<ContentWarning>?,
    override val deactivated: Boolean,
    override val activeVideoId: VideoId?,
    override val categories: Map<CategorySource, Set<CategoryWithAncestors>>,
    override val analysisFailed: Boolean = false,
) : BaseVideo {
    override fun isPlayable(): Boolean {
        return playback !is VideoPlayback.FaultyPlayback
    }

    override fun isBoclipsHosted(): Boolean {
        return playback is VideoPlayback.StreamPlayback
    }

    override fun getRatingAverage() = when {
        this.ratings.isEmpty() -> null
        else -> this.ratings.map { it.rating }.average()
    }

    override fun isRatedByUser(user: UserId) =
        ratings.any { it.userId == user }

    override fun hasTranscript(): Boolean {
        return this.voice.transcript?.content != null
    }

    override fun toString(): String {
        return "Video(videoId=$videoId, title='$title', channelName='${channel.name}', videoReference='$videoReference')"
    }

    override fun isVoiced(): Boolean? {
        return voice.isVoiced()
    }

    val channelCategories = this.categories.let { it[CategorySource.CHANNEL] } ?: emptySet()

    val manualCategories = this.categories.let { it[CategorySource.MANUAL] } ?: emptySet()
}
