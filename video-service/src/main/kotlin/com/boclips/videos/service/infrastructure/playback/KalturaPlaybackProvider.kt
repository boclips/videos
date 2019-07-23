package com.boclips.videos.service.infrastructure.playback

import com.boclips.eventbus.events.video.Captions
import com.boclips.kalturaclient.KalturaClient
import com.boclips.kalturaclient.captionasset.KalturaLanguage
import com.boclips.kalturaclient.http.KalturaClientApiException
import com.boclips.kalturaclient.media.MediaEntry
import com.boclips.kalturaclient.media.MediaEntryStatus
import com.boclips.kalturaclient.media.streams.StreamFormat
import com.boclips.videos.service.domain.model.playback.PlaybackId
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
        val kalturaVideoIds = playbackIds.map { playbackId -> playbackId.value }
        val mediaEntriesById = kalturaClient.getMediaEntriesByReferenceIds(kalturaVideoIds)

        return playbackIds
            .asSequence()
            .filter { id ->
                val kalturaVideoId = id.value
                mediaEntriesById[kalturaVideoId] != null
            }
            .filter { kalturaVideoId -> filterValidMediaEntries(kalturaVideoId, mediaEntriesById) != null }
            .mapNotNull { kalturaVideoId ->
                val optionalMediaEntry = filterValidMediaEntries(kalturaVideoId, mediaEntriesById)

                val mediaEntry = optionalMediaEntry!!
                val videoPlayback = StreamPlayback(
                    id = kalturaVideoId,
                    duration = mediaEntry.duration,
                    appleHlsStreamUrl = mediaEntry.streams.withFormat(StreamFormat.APPLE_HDS),
                    mpegDashStreamUrl = mediaEntry.streams.withFormat(StreamFormat.MPEG_DASH),
                    progressiveDownloadStreamUrl = mediaEntry.streams.withFormat(StreamFormat.PROGRESSIVE_DOWNLOAD),
                    thumbnailUrl = mediaEntry.thumbnailUrl,
                    downloadUrl = mediaEntry.downloadUrl
                )

                (kalturaVideoId to videoPlayback)
            }
            .toMap()
    }

    override fun removePlayback(playbackId: PlaybackId) {
        try {
            kalturaClient.deleteMediaEntriesByReferenceId(playbackId.value)
        } catch (ex: KalturaClientApiException) {
            logger.error { "Failed to execute video from Kaltura: $ex" }
        }
    }

    override fun uploadCaptions(playbackId: PlaybackId, captions: Captions) {
        val captionAsset = getCaptionAsset(captions)
        logger.info { "Checking existing captions for ref id ${playbackId.value}" }

        deleteExistingAutoGeneratedCaptionsForLanguage(captionAsset.language, playbackId.value)

        if (hasCaptionsForLanguage(captionAsset.language, playbackId.value)) {
            logger.info { "Skipping captions upload for ref id ${playbackId.value} because there already are ${captionAsset.language} captions in Kaltura" }
            return
        }

        logger.info { "Uploading ${captionAsset.language} captions for ref id ${playbackId.value}" }
        val uploadedAsset = kalturaClient.createCaptionsFile(playbackId.value, captionAsset, captions.content)
        logger.info { "Uploaded ${captionAsset.language} captions for ref id ${playbackId.value}: ${uploadedAsset.id}" }
    }

    override fun deleteAutoGeneratedCaptions(playbackId: PlaybackId, language: Locale) {
        val languageName = language.getDisplayLanguage(Locale.ENGLISH)
        logger.info { "Deleting existing $languageName captions for ref id ${playbackId.value}" }
        val kalturaLanguage = KalturaLanguage.fromName(languageName)
        deleteExistingAutoGeneratedCaptionsForLanguage(kalturaLanguage, playbackId.value)
    }

    override fun retrieveProviderMetadata(playbackIds: List<PlaybackId>): Map<PlaybackId, VideoProviderMetadata> {
        return playbackIds.map { it to VideoProviderMetadata.KalturaMetadata(id = it) }.toMap()
    }

    private fun deleteExistingAutoGeneratedCaptionsForLanguage(language: KalturaLanguage, referenceId: String) {
        kalturaClient.getCaptionFilesByReferenceId(referenceId)
            .find { video -> video.language == language && video.label.contains("(auto-generated)") }
            ?.let { video ->
                logger.info { "There are already auto-generated ${language.getName()} captions for reference id $referenceId. Deleting." }
                kalturaClient.deleteCaptionContentByAssetId(video.id)
            }
    }

    private fun hasCaptionsForLanguage(language: KalturaLanguage, referenceId: String): Boolean {
        return kalturaClient.getCaptionFilesByReferenceId(referenceId)
            .any { video -> video.language == language }
    }

    private fun filterValidMediaEntries(id: PlaybackId, mediaEntriesById: Map<String, List<MediaEntry>>) =
        mediaEntriesById.getValue(id.value).firstOrNull { it.status == MediaEntryStatus.READY }
}
