package com.boclips.videos.api.request

import com.boclips.videos.api.common.IngestType
import com.boclips.videos.api.request.channel.ChannelFilterRequest
import com.boclips.videos.api.request.channel.ChannelRequest
import com.boclips.videos.api.request.channel.ContentCategoryRequest
import com.boclips.videos.api.request.channel.LegalRestrictionsRequest
import com.boclips.videos.api.request.channel.MarketingInformationRequest
import com.boclips.videos.api.request.collection.CreateCollectionRequest
import com.boclips.videos.api.request.subject.CreateSubjectRequest
import com.boclips.videos.api.request.tag.CreateTagRequest
import com.boclips.videos.api.request.video.CreateVideoRequest
import com.boclips.videos.api.request.video.UpdateVideoRequest
import com.boclips.videos.api.response.channel.DistributionMethodResource
import com.boclips.videos.api.response.channel.IngestDetailsResource
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
            videoTypes: List<String>? = listOf("NEWS"),
            playbackId: String? = "123",
            playbackProvider: String? = "KALTURA",
            analyseVideo: Boolean = false,
            subjects: Set<String> = setOf(),
            youtubeChannelId: String = "1234",
            language: String? = "cym",
            isVoiced: Boolean? = null,
            categories: List<String>? = null
        ) = CreateVideoRequest(
            providerId = providerId,
            providerVideoId = providerVideoId,
            title = title,
            description = description,
            releasedOn = releasedOn,
            legalRestrictions = legalRestrictions,
            keywords = keywords,
            videoTypes = videoTypes,
            playbackId = playbackId,
            playbackProvider = playbackProvider,
            analyseVideo = analyseVideo,
            subjects = subjects,
            youtubeChannelId = youtubeChannelId,
            language = language,
            isVoiced = isVoiced,
            categories = categories,
        )

        @JvmStatic
        fun createCollectionRequest(
            title: String? = "collection title",
            description: String? = null,
            videos: List<String> = listOf(),
            discoverable: Boolean? = null
        ) = CreateCollectionRequest(
            title = title,
            description = description,
            videos = videos,
            discoverable = discoverable
        )

        @JvmStatic
        fun createUpdateVideoRequest(
            title: String? = "video-title",
            description: String? = "description",
            additionalDescription: String? = "additional description",
            promoted: Boolean? = false,
            subjectIds: List<String>? = null,
            ageRangeMin: Int? = null,
            ageRangeMax: Int? = null,
            rating: Int? = null,
            categories: List<String>? = emptyList(),
        ): UpdateVideoRequest {
            return UpdateVideoRequest(
                title = title,
                description = description,
                additionalDescription = additionalDescription,
                promoted = promoted,
                subjectIds = subjectIds,
                ageRangeMin = ageRangeMin,
                ageRangeMax = ageRangeMax,
                rating = rating,
                categories = categories
            )
        }

        @JvmStatic
        fun createChannelRequest(
            name: String? = "TED",
            ageRanges: List<String>? = emptyList(),
            distributionMethods: Set<DistributionMethodResource>? = null,
            legalRestrictions: LegalRestrictionsRequest? = null,
            currency: String? = null,
            description: String? = null,
            contentCategories: List<ContentCategoryRequest>? = null,
            notes: String? = null,
            language: String? = null,
            ingest: IngestDetailsResource? = IngestDetailsResource(
                type = IngestType.YOUTUBE,
                playlistIds = listOf("yt-id")
            ),
            oneLineDescription: String? = null,
            marketingInformation: MarketingInformationRequest? = null,
            isTranscriptProvided: Boolean? = null,
            educationalResources: String? = null,
            curriculumAligned: String? = null,
            bestForTags: List<String>? = null,
            subjects: List<String>? = null,
            contentTypes: List<String>? = null,
            contractId: String? = null,
            categories: List<String>? = null,
            requiresVideoLevelTagging: Boolean? = null,
            private: Boolean? = null
        ): ChannelRequest {
            return ChannelRequest(
                name = name,
                ageRanges = ageRanges,
                distributionMethods = distributionMethods,
                legalRestrictions = legalRestrictions,
                currency = currency,
                description = description,
                contentCategories = contentCategories,
                notes = notes,
                language = language,
                ingest = ingest,
                oneLineDescription = oneLineDescription,
                marketingInformation = marketingInformation,
                isTranscriptProvided = isTranscriptProvided,
                educationalResources = educationalResources,
                curriculumAligned = curriculumAligned,
                bestForTags = bestForTags,
                subjects = subjects,
                contentTypes = contentTypes,
                contractId = contractId,
                categories = categories,
                requiresVideoLevelTagging = requiresVideoLevelTagging,
                private = private
            )
        }

        @JvmStatic
        fun channelFilterRequest(
            name: String? = null
        ) = ChannelFilterRequest(
            name = name
        )
    }
}
