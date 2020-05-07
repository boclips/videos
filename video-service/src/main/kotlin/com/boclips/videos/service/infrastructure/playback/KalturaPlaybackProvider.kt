package com.boclips.videos.service.infrastructure.playback

import com.boclips.eventbus.domain.video.Captions
import com.boclips.kalturaclient.KalturaClient
import com.boclips.kalturaclient.captionasset.CaptionAsset
import com.boclips.kalturaclient.captionasset.KalturaLanguage
import com.boclips.kalturaclient.flavorAsset.Asset
import com.boclips.kalturaclient.http.KalturaClientApiException
import com.boclips.kalturaclient.media.MediaEntryStatus
import com.boclips.videos.service.domain.model.playback.Dimensions
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType.KALTURA
import com.boclips.videos.service.domain.model.playback.VideoPlayback.StreamPlayback
import com.boclips.videos.service.domain.model.playback.VideoProviderMetadata
import com.boclips.videos.service.domain.model.video.VideoAsset
import com.boclips.videos.service.domain.service.video.plackback.PlaybackProvider
import com.boclips.videos.service.infrastructure.playback.CaptionAssetConverter.getCaptionAsset
import mu.KLogging
import java.util.*

class KalturaPlaybackProvider(private val kalturaClient: KalturaClient) :
    PlaybackProvider {
    companion object : KLogging()

    override fun retrievePlayback(playbackIds: List<PlaybackId>): Map<PlaybackId, StreamPlayback> {

        val entryIds = playbackIds.map { it.value }

        if (entryIds.isEmpty()) {
            return emptyMap()
        }

        val assetsByEntryId = kalturaClient.getVideoAssets(entryIds)

        return kalturaClient.getEntries(entryIds)
            .map { PlaybackId(KALTURA, it.key) to it.value }.toMap()
            .asSequence()
            .filter { it.value.status == MediaEntryStatus.READY }
            .mapNotNull {
                val videoPlayback = StreamPlayback(
                    id = PlaybackId(type = KALTURA, value = it.value.id),
                    referenceId = it.value.referenceId,
                    duration = it.value.duration,
                    downloadUrl = it.value.downloadUrl,
                    createdAt = it.value.createdAt,
                    assets = convertAndValidateAssetsToSet(assetsByEntryId[it.value.id]),
                    originalDimensions = Dimensions(
                        width = it.value.width,
                        height = it.value.height
                    )
                )
                (it.key to videoPlayback)
            }
            .toMap()
    }

    override fun removePlayback(playbackId: PlaybackId) {
        try {
            kalturaClient.deleteEntry(playbackId.value)
        } catch (ex: KalturaClientApiException) {
            logger.error { "Failed to execute video from Kaltura: $ex" }
        }
    }

    override fun uploadCaptions(playbackId: PlaybackId, captions: Captions) {
        val captionAsset = getCaptionAsset(captions)
        logger.info { "Checking existing captions for ref id ${playbackId.value}" }

        deleteExistingAutoGeneratedCaptionsForLanguage(captionAsset.language, playbackId)

        if (hasCaptionsForLanguage(captionAsset.language, playbackId)) {
            logger.info { "Skipping captions upload for ref id ${playbackId.value} because there already are ${captionAsset.language} captions in Kaltura" }
            return
        }

        logger.info { "Uploading ${captionAsset.language} captions for ref id ${playbackId.value}" }

        val uploadedAsset =
            kalturaClient.createCaptionForVideo(playbackId.value, captionAsset, captions.content)

        logger.info { "Uploaded ${captionAsset.language} captions for ref id ${playbackId.value}: ${uploadedAsset.id}" }
    }

    override fun overwriteCaptionContent(playbackId: PlaybackId, content: String) {
        retrieveCaptionFiles(playbackId = playbackId).first().let { captions ->
            kalturaClient.deleteCaption(captions.id)
            kalturaClient.createCaptionForVideo(playbackId.value, captions, content)
        }
    }

    override fun getCaptionContent(playbackId: PlaybackId): String? {
        return retrieveCaptionFiles(playbackId = playbackId).first().let { captions ->
            kalturaClient.getCaptionContent(captions.id)
        }
    }

    override fun deleteAutoGeneratedCaptions(playbackId: PlaybackId, language: Locale) {
        val languageName = language.getDisplayLanguage(Locale.ENGLISH)
        logger.info { "Deleting existing $languageName captions for $playbackId" }
        val kalturaLanguage = KalturaLanguage.fromName(languageName)
        deleteExistingAutoGeneratedCaptionsForLanguage(kalturaLanguage, playbackId)
    }

    override fun retrieveProviderMetadata(playbackIds: List<PlaybackId>): Map<PlaybackId, VideoProviderMetadata> {
        return playbackIds.map { it to VideoProviderMetadata.KalturaMetadata(id = it) }.toMap()
    }

    private fun deleteExistingAutoGeneratedCaptionsForLanguage(language: KalturaLanguage, playbackId: PlaybackId) {
        retrieveCaptionFiles(playbackId)
            .find { video -> video.language == language && video.label.contains("(auto-generated)") }
            ?.let { video ->
                logger.info { "There are already auto-generated ${language.getName()} captions for reference id ${playbackId.value}. Deleting." }
                kalturaClient.deleteCaption(video.id)
            }
    }

    private fun hasCaptionsForLanguage(language: KalturaLanguage, playbackId: PlaybackId): Boolean {
        return retrieveCaptionFiles(playbackId)
            .any { video -> video.language == language }
    }

    private fun retrieveCaptionFiles(playbackId: PlaybackId): List<CaptionAsset> {
        return kalturaClient.getCaptionsForVideo(playbackId.value)
    }

    private fun convertAndValidateAssetsToSet(kalturaAssets: List<Asset>?): Set<VideoAsset> {
        return kalturaAssets.orEmpty()
            .filter { it.sizeKb != 0 }
            .map(VideoAssetConverter::convert)
            .toSet()
    }
}
