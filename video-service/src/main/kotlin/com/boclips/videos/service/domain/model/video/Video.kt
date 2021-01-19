package com.boclips.videos.service.domain.model.video

import com.boclips.videos.service.domain.model.AgeRange
import com.boclips.videos.service.domain.model.attachment.Attachment
import com.boclips.videos.service.domain.model.contentwarning.ContentWarning
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.domain.model.tag.UserTag
import com.boclips.videos.service.domain.model.user.Deal
import com.boclips.videos.service.domain.model.user.Organisation
import com.boclips.videos.service.domain.model.user.OrganisationId
import com.boclips.videos.service.domain.model.user.UserId
import com.boclips.videos.service.domain.model.video.channel.Channel
import java.time.LocalDate
import java.time.ZonedDateTime
import com.boclips.videos.service.domain.model.user.Deal.Prices as OrganisationPrices

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
    override val activeVideoId: VideoId?

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
        return this.voice.transcript != null
    }

    override fun toString(): String {
        return "Video(videoId=$videoId, title='$title', channelName='${channel.name}', videoReference='$videoReference')"
    }

    override fun isVoiced(): Boolean? {
        return voice.isVoiced()
    }

    fun getPrice(organisationPrices: OrganisationPrices?): Price? {
        return if (isBoclipsHosted()) {
            if (types.isEmpty()) throw VideoMissingTypeException(videoId)
            Price.computePrice(types, organisationPrices)
        } else {
            null
        }
    }

    fun getPrices(organisationsPrices: List<Organisation>): Map<OrganisationId, Price> {
        return emptyMap()
    }
}
