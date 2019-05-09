package com.boclips.videos.service.presentation.video

import com.boclips.videos.service.application.exceptions.NonNullableFieldCreateRequestException.Companion.getOrThrow
import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.asset.LegacySubject
import com.boclips.videos.service.domain.model.asset.LegacyVideoType
import com.boclips.videos.service.domain.model.asset.VideoAsset
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import org.bson.types.ObjectId

class CreateVideoRequestToAssetConverter {

    fun convert(
        createVideoRequest: CreateVideoRequest,
        videoPlayback: VideoPlayback
    ): VideoAsset {
        return VideoAsset(
            assetId = AssetId(value = ObjectId().toHexString()),
            playbackId = PlaybackId(
                PlaybackProviderType.valueOf(getOrThrow(createVideoRequest.playbackProvider, "playback provider")),
                getOrThrow(createVideoRequest.playbackId, "playback id")
            ),
            playback = null,
            title = getOrThrow(createVideoRequest.title, "title"),
            description = getOrThrow(createVideoRequest.description, "description"),
            keywords = getOrThrow(createVideoRequest.keywords, "keywords"),
            releasedOn = getOrThrow(createVideoRequest.releasedOn, "releasedOn"),
            contentPartnerId = getOrThrow(createVideoRequest.provider, "contentPartnerId"),
            contentPartnerVideoId = getOrThrow(createVideoRequest.providerVideoId, "contentPartnerVideoId"),
            type = LegacyVideoType.valueOf(getOrThrow(createVideoRequest.videoType, "content type")),
            duration = videoPlayback.duration,
            legalRestrictions = createVideoRequest.legalRestrictions ?: "",
            subjects = getOrThrow(createVideoRequest.subjects, "subjects").map { LegacySubject(it) }.toSet(),
            language = null,
            transcript = null,
            topics = emptySet(),
            searchable = createVideoRequest.searchable ?: true
        )
    }
}