package com.boclips.videos.service.infrastructure.playback

import com.boclips.eventbus.domain.video.Captions
import com.boclips.kalturaclient.KalturaClient
import com.boclips.kalturaclient.captionasset.CaptionAsset
import com.boclips.kalturaclient.captionasset.KalturaLanguage
import com.boclips.kalturaclient.http.KalturaClientApiException
import com.boclips.kalturaclient.media.MediaEntry
import com.boclips.kalturaclient.media.MediaEntryStatus
import com.boclips.kalturaclient.media.streams.StreamFormat
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType.KALTURA
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType.KALTURA_REFERENCE
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType.YOUTUBE
import com.boclips.videos.service.domain.model.playback.VideoPlayback.StreamPlayback
import com.boclips.videos.service.domain.model.playback.VideoProviderMetadata
import com.boclips.videos.service.domain.service.video.PlaybackProvider
import com.boclips.videos.service.infrastructure.playback.CaptionAssetConverter.getCaptionAsset
import mu.KLogging
import java.util.Locale

class KalturaPlaybackProvider(private val kalturaClient: KalturaClient) :
    PlaybackProvider {
    companion object : KLogging()

    override fun retrievePlayback(playbackIds: List<PlaybackId>): Map<PlaybackId, StreamPlayback> {
        return retrieveMediaEntries(playbackIds)
            .asSequence()
            .filter { it.value.status == MediaEntryStatus.READY }
            .mapNotNull {
                val videoPlayback = StreamPlayback(
                    id = PlaybackId(type = KALTURA, value = it.value.id),
                    referenceId = it.value.referenceId,
                    duration = it.value.duration,
                    appleHlsStreamUrl = it.value.streams.withFormat(StreamFormat.APPLE_HDS),
                    mpegDashStreamUrl = it.value.streams.withFormat(StreamFormat.MPEG_DASH),
                    progressiveDownloadStreamUrl = it.value.streams.withFormat(StreamFormat.PROGRESSIVE_DOWNLOAD),
                    thumbnailUrl = it.value.thumbnailUrl,
                    downloadUrl = it.value.downloadUrl
                )

                (it.key to videoPlayback)
            }
            .toMap()
    }

    override fun removePlayback(playbackId: PlaybackId) {
        try {
            if (KALTURA == playbackId.type) {
                kalturaClient.deleteMediaEntryById(playbackId.value)
            } else {
                kalturaClient.deleteMediaEntriesByReferenceId(playbackId.value)
            }
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
        val uploadedAsset = kalturaClient.createCaptionsFile(playbackId.value, captionAsset, captions.content)
        logger.info { "Uploaded ${captionAsset.language} captions for ref id ${playbackId.value}: ${uploadedAsset.id}" }
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
                kalturaClient.deleteCaptionContentByAssetId(video.id)
            }
    }

    private fun hasCaptionsForLanguage(language: KalturaLanguage, playbackId: PlaybackId): Boolean {
        return retrieveCaptionFiles(playbackId)
            .any { video -> video.language == language }
    }

    private fun retrieveCaptionFiles(playbackId: PlaybackId): List<CaptionAsset> {
        return if (KALTURA == playbackId.type) {
            kalturaClient.getCaptionFilesByEntryId(playbackId.value)
        } else {
            kalturaClient.getCaptionFilesByReferenceId(playbackId.value)
        }
    }
    private fun retrieveMediaEntries(playbackIds: List<PlaybackId>): Map<PlaybackId, MediaEntry> {
        val mediaEntriesById = retrieveMediaEntriesByType(playbackIds, KALTURA)
        val mediaEntriesByReferenceId = retrieveMediaEntriesByType(playbackIds, KALTURA_REFERENCE)

        return mediaEntriesById.plus(mediaEntriesByReferenceId)
    }

    private fun retrieveMediaEntriesByType(
        playbackIds: List<PlaybackId>,
        playbackProviderType: PlaybackProviderType
    ): Map<PlaybackId, MediaEntry> {
        val filteredIds = playbackIds.filter { playbackProviderType == it.type }.map { it.value }

        val mediaEntries: Map<String, MediaEntry> = when (playbackProviderType) {
            KALTURA -> kalturaClient.getMediaEntriesByIds(filteredIds)
            KALTURA_REFERENCE -> kalturaClient.getMediaEntriesByReferenceIds(filteredIds)
                .filter { it.value.isNotEmpty() }
                .map { (it.key to it.value.first()) }.toMap()
            YOUTUBE -> throw IllegalArgumentException("Playback Provider Type must be Kaltura")
        }

        return mediaEntries
            .map { PlaybackId(playbackProviderType, it.key) to it.value }.toMap()
    }
}
