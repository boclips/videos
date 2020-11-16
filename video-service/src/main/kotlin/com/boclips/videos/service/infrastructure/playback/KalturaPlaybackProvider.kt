package com.boclips.videos.service.infrastructure.playback

import com.boclips.eventbus.domain.video.Captions
import com.boclips.kalturaclient.KalturaCaptionManager
import com.boclips.kalturaclient.KalturaClient
import com.boclips.kalturaclient.captionasset.CaptionAsset
import com.boclips.kalturaclient.captionasset.KalturaLanguage
import com.boclips.kalturaclient.flavorAsset.Asset
import com.boclips.kalturaclient.http.KalturaClientApiException
import com.boclips.kalturaclient.media.MediaEntryStatus
import com.boclips.videos.service.application.video.exceptions.VideoPlaybackNotFound
import com.boclips.videos.service.domain.model.playback.CaptionConflictException
import com.boclips.videos.service.domain.model.playback.Dimensions
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType.KALTURA
import com.boclips.videos.service.domain.model.playback.VideoPlayback.StreamPlayback
import com.boclips.videos.service.domain.model.playback.VideoProviderMetadata
import com.boclips.videos.service.domain.model.video.Caption
import com.boclips.videos.service.domain.model.video.CaptionFormat
import com.boclips.videos.service.domain.model.video.UnknownCaptionFormatException
import com.boclips.videos.service.domain.model.video.VideoAsset
import com.boclips.videos.service.domain.service.video.plackback.PlaybackProvider
import com.boclips.videos.service.infrastructure.playback.CaptionAssetConverter.getCaptionAsset
import mu.KLogging
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpMethod
import java.io.OutputStream
import java.net.URI
import java.util.Locale
import com.boclips.kalturaclient.captionasset.CaptionFormat as KalturaCaptionFormat

class KalturaPlaybackProvider(
    private val kalturaClient: KalturaClient,
    restTemplateBuilder: RestTemplateBuilder
) :
    PlaybackProvider {
    companion object : KLogging()

    val restTemplate = restTemplateBuilder.build()

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
                    thumbnailSecond = null,
                    customThumbnail = false,
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

    override fun getCaptions(playbackId: PlaybackId): List<Caption> {
        return retrieveCaptionFiles(playbackId = playbackId).map { captions ->
            Caption(
                content = kalturaClient.getCaptionContent(captions.id),
                default = captions.isDefaultCaption,
                format = when (captions.fileType) {
                    KalturaCaptionFormat.SRT -> CaptionFormat.SRT
                    KalturaCaptionFormat.DFXP -> CaptionFormat.DFXP
                    KalturaCaptionFormat.WEBVTT -> CaptionFormat.WEBVTT
                    KalturaCaptionFormat.CAP -> CaptionFormat.CAP
                    else -> throw UnknownCaptionFormatException(playbackId, captions.fileType)
                }
            )
        }
    }

    override fun requestCaptions(playbackId: PlaybackId) {
        val captionStatus = kalturaClient.getCaptionStatus(playbackId.value)
        if (captionStatus == KalturaCaptionManager.CaptionStatus.NOT_AVAILABLE ||
            captionStatus == KalturaCaptionManager.CaptionStatus.AUTO_GENERATED_AVAILABLE
        ) {
            kalturaClient.requestCaption(playbackId.value)
        } else {
            logger.info { "requestCaptions: captions for playback id: ${playbackId.value} have status: $captionStatus" }
            throw CaptionConflictException("Captions for playback id: ${playbackId.value} have already been requested")
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

    override fun downloadHighestResolutionVideo(
        playbackId: PlaybackId,
        outputStream: OutputStream
    ) {
        val downloadAssetUrl = getDownloadAssetUrl(playbackId)
        restTemplate.execute<Long>(
            downloadAssetUrl, HttpMethod.GET, null,
            { response ->
                response.body.copyTo(outputStream)
            }
        )
    }

    override fun getExtensionForAsset(playbackId: PlaybackId): String =
        getDownloadAssetUrl(playbackId).toString().substringAfterLast('.', "")

    override fun getDownloadAssetUrl(playbackId: PlaybackId): URI {
        val asset = kalturaClient.getVideoAssets(playbackId.value)
            ?.maxByOrNull { it.height }
            ?: throw VideoPlaybackNotFound(playbackId)

        return kalturaClient.getDownloadAssetUrl(asset.id)
    }

    override fun getHumanGeneratedCaptionUrl(playbackId: PlaybackId): URI? {
        return kalturaClient.getHumanGeneratedCaptionAsset(playbackId.value)
            ?.let { kalturaClient.getCaptionAssetUrl(it.id) }
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
