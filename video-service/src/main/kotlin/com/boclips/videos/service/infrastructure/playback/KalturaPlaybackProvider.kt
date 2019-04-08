package com.boclips.videos.service.infrastructure.playback

import com.boclips.events.types.Captions
import com.boclips.kalturaclient.KalturaClient
import com.boclips.kalturaclient.http.KalturaClientApiException
import com.boclips.kalturaclient.media.MediaEntry
import com.boclips.kalturaclient.media.MediaEntryStatus
import com.boclips.kalturaclient.media.streams.StreamFormat
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.StreamPlayback
import com.boclips.videos.service.domain.service.video.PlaybackProvider
import com.boclips.videos.service.infrastructure.playback.CaptionAssetConverter.getCaptionAsset
import mu.KLogging

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
                val mediaEntry = filterValidMediaEntries(kalturaVideoId, mediaEntriesById)

                val streamUrl = mediaEntry!!.streams.withFormat(StreamFormat.APPLE_HDS)
                val videoPlayback = StreamPlayback(
                    id = kalturaVideoId,
                    thumbnailUrl = mediaEntry.thumbnailUrl,
                    duration = mediaEntry.duration,
                    streamUrl = streamUrl,
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
            logger.error { "Failed to execute asset from Kaltura: $ex" }
        }
    }

    override fun uploadCaptions(playbackId: PlaybackId, captions: Captions) {
        val captionAsset = getCaptionAsset(captions)
        logger.info { "Checking existing captions for ref is ${playbackId.value}" }
        val existingCaptions = kalturaClient.getCaptionFilesByReferenceId(playbackId.value)
        if(existingCaptions.any { it.language == captionAsset.language }) {
            logger.info { "Skipping captions upload for ref id ${playbackId.value} because there already are ${captionAsset.language} captions in Kaltura" }
            return
        }

        logger.info { "Uploading ${captionAsset.language} captions for ref id ${playbackId.value}" }
        val uploadedAsset = kalturaClient.createCaptionsFile(playbackId.value, captionAsset, captions.content)
        logger.info { "Uploaded ${captionAsset.language} captions for ref id ${playbackId.value}: ${uploadedAsset.id}" }
    }

    private fun filterValidMediaEntries(id: PlaybackId, mediaEntriesById: Map<String, List<MediaEntry>>) =
        mediaEntriesById.getValue(id.value).firstOrNull { it.status == MediaEntryStatus.READY }
}
