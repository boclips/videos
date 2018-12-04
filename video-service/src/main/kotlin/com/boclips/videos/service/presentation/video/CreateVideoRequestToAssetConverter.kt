package com.boclips.videos.service.presentation.video

import com.boclips.videos.service.application.video.exceptions.InvalidCreateVideoRequestException
import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.asset.VideoAsset
import com.boclips.videos.service.domain.model.asset.VideoType
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType

class CreateVideoRequestToAssetConverter {

    fun convert(createVideoRequest: CreateVideoRequest): VideoAsset {
        validateObligatoryField("playback provider", createVideoRequest.playbackProvider)
        validateObligatoryField("playback id", createVideoRequest.playbackId)
        validateObligatoryField("title", createVideoRequest.title)
        validateObligatoryField("description", createVideoRequest.description)
        validateObligatoryField("keywords", createVideoRequest.keywords)
        validateObligatoryField("duration", createVideoRequest.duration)
        validateObligatoryField("releasedOn", createVideoRequest.releasedOn)
        validateObligatoryField("contentProvider", createVideoRequest.provider)
        validateObligatoryField("contentProviderId", createVideoRequest.providerVideoId)
        validateObligatoryField("content type", createVideoRequest.contentType)
        validateObligatoryField("restrictions", createVideoRequest.legalRestrictions)

        return VideoAsset(
                assetId = AssetId(""),
                playbackId = PlaybackId(PlaybackProviderType.valueOf(createVideoRequest.playbackProvider!!), createVideoRequest.playbackId!!),
                title = createVideoRequest.title!!,
                description = createVideoRequest.description!!,
                keywords = createVideoRequest.keywords!!,
                releasedOn = createVideoRequest.releasedOn!!,
                contentProvider = createVideoRequest.provider!!,
                contentProviderId = createVideoRequest.providerVideoId!!,
                type = VideoType.valueOf(createVideoRequest.contentType!!),
                duration = createVideoRequest.duration!!,
                legalRestrictions = createVideoRequest.legalRestrictions!!
        )
    }

    private fun <T> validateObligatoryField(fieldName: String, fieldValue: T?) {
        fieldValue ?: throw InvalidCreateVideoRequestException("$fieldName cannot be null")
    }

}