package com.boclips.videos.service.domain.service.video.plackback

import com.boclips.eventbus.domain.video.Captions
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.domain.model.playback.VideoProviderMetadata
import com.boclips.videos.service.domain.model.video.Caption
import java.io.OutputStream
import java.net.URI
import java.util.Locale

interface PlaybackProvider {
    fun retrievePlayback(playbackIds: List<PlaybackId>): Map<PlaybackId, VideoPlayback>
    fun removePlayback(playbackId: PlaybackId)
    fun uploadCaptions(playbackId: PlaybackId, captions: Captions)
    fun overwriteCaptionContent(playbackId: PlaybackId, content: String)
    fun getCaptions(playbackId: PlaybackId): List<Caption>
    fun requestCaptions(playbackId: PlaybackId)
    fun deleteAutoGeneratedCaptions(playbackId: PlaybackId, language: Locale)
    fun retrieveProviderMetadata(playbackIds: List<PlaybackId>): Map<PlaybackId, VideoProviderMetadata>
    fun downloadHighestResolutionVideo(playbackId: PlaybackId, outputStream: OutputStream)
    fun getExtensionForAsset(playbackId: PlaybackId): String
    fun getDownloadAssetUrl(playbackId: PlaybackId): URI
    fun getSrtCaptionsUrl(playbackId: PlaybackId): URI?
}
