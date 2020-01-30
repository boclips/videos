package com.boclips.videos.api.request

import com.boclips.videos.api.request.collection.CreateCollectionRequest
import com.boclips.videos.api.request.contentpartner.AgeRangeRequest
import com.boclips.videos.api.request.contentpartner.ContentPartnerFilterRequest
import com.boclips.videos.api.request.contentpartner.CreateContentPartnerRequest
import com.boclips.videos.api.request.contentpartner.LegalRestrictionsRequest
import com.boclips.videos.api.request.subject.CreateSubjectRequest
import com.boclips.videos.api.request.tag.CreateTagRequest
import com.boclips.videos.api.request.video.CreateVideoRequest
import com.boclips.videos.api.request.video.UpdateVideoRequest
import com.boclips.videos.api.response.contentpartner.DistributionMethodResource
import java.time.LocalDate

class VideoServiceApiFactory {
    companion object {
        @JvmStatic
        fun createSubjectRequest(name: String? = null): CreateSubjectRequest = CreateSubjectRequest(name = name)

        @JvmStatic
        fun createTagRequest(label: String? = null): CreateTagRequest = CreateTagRequest(label = label)

        @JvmStatic
        fun createCreateVideoRequest(
            providerVideoId: String? = "AP-1",
            providerId: String? = "provider-id",
            title: String? = "an AP video",
            description: String? = "an AP video about penguins",
            releasedOn: LocalDate? = LocalDate.now(),
            legalRestrictions: String? = "None",
            keywords: List<String>? = listOf("k1", "k2"),
            videoType: String? = "NEWS",
            playbackId: String? = "123",
            playbackProvider: String? = "KALTURA",
            analyseVideo: Boolean = false,
            subjects: Set<String> = setOf(),
            youtubeChannelId: String = "1234",
            language: String? = "cym"
        ) = CreateVideoRequest(
            providerId = providerId,
            providerVideoId = providerVideoId,
            title = title,
            description = description,
            releasedOn = releasedOn,
            legalRestrictions = legalRestrictions,
            keywords = keywords,
            videoType = videoType,
            playbackId = playbackId,
            playbackProvider = playbackProvider,
            analyseVideo = analyseVideo,
            subjects = subjects,
            youtubeChannelId = youtubeChannelId,
            language = language
        )

        @JvmStatic
        fun createCollectionRequest(
            title: String? = "collection title",
            description: String? = null,
            videos: List<String> = listOf(),
            public: Boolean? = null
        ) = CreateCollectionRequest(
            title = title,
            description = description,
            videos = videos,
            public = public
        )

        @JvmStatic
        fun createUpdateVideoRequest(
            title: String? = "video-title",
            description: String? = "description",
            promoted: Boolean? = false,
            subjectIds: List<String>? = emptyList()
        ): UpdateVideoRequest {
            return UpdateVideoRequest(
                title = title,
                description = description,
                promoted = promoted,
                subjectIds = subjectIds
            )
        }

        @JvmStatic
        fun createContentPartnerRequest(
            name: String? = "TED",
            ageRange: AgeRangeRequest? = AgeRangeRequest(
                min = 5,
                max = 11
            ),
            accreditedToYtChannel: String? = null,
            distributionMethods: Set<DistributionMethodResource>? = null,
            legalRestrictions: LegalRestrictionsRequest? = null,
            currency: String? = null,
            description: String? = null,
            contentCategories: List<String>? = null,
            hubspotId: String? = null,
            awards: String? = null,
            notes: String? = null,
            language: String? = null
        ): CreateContentPartnerRequest {
            return CreateContentPartnerRequest(
                name = name,
                ageRange = ageRange,
                accreditedToYtChannelId = accreditedToYtChannel,
                distributionMethods = distributionMethods,
                legalRestrictions = legalRestrictions,
                currency = currency,
                description = description,
                contentCategories = contentCategories,
                hubspotId = hubspotId,
                awards = awards,
                notes = notes,
                language = language
            )
        }

        @JvmStatic
        fun contentPartnerFilterRequest(
            name: String? = null,
            official: Boolean? = null,
            accreditedToYtChannel: String? = null
        ) = ContentPartnerFilterRequest(
            name = name,
            official = official,
            accreditedToYtChannelId = accreditedToYtChannel
        )
    }
}
