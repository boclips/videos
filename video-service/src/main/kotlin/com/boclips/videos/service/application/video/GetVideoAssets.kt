package com.boclips.videos.service.application.video

import com.boclips.videos.service.application.video.exceptions.VideoCaptionNotFound
import com.boclips.videos.service.application.video.exceptions.VideoPlaybackNotFound
import com.boclips.videos.service.application.video.search.SearchVideo
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.model.video.Caption
import com.boclips.videos.service.domain.model.video.InsufficientVideoResolutionException
import com.boclips.videos.service.domain.model.video.UnsupportedCaptionsException
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.service.video.CaptionService
import com.boclips.videos.service.domain.service.video.plackback.PlaybackProvider
import mu.KLogging
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class GetVideoAssets(
    private val captionService: CaptionService,
    private val searchVideo: SearchVideo,
    private val playbackProvider: PlaybackProvider
) {
    companion object : KLogging() {
        fun buildFilename(title: String) =
            title
                .replace(Regex("[^A-Za-z\\s\\d]+"), "")
                .replace(Regex("[\\s]+"), "-")
    }

    operator fun invoke(videoId: String, user: User): ResponseEntity<StreamingResponseBody> {
        searchVideo.byId(videoId, user).let { video ->
            validateVideoIsDownloadable(video)
            val captions = getDefaultCaptions(video)
            return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"${buildFilename(video.title)}.zip\"")
                .contentType(MediaType("application", "zip"))
                .body(StreamingResponseBody { writeCompressedContent(it, video, captions) })
        }
    }

    private fun validateVideoIsDownloadable(
        video: Video
    ) {
        if (video.playback is VideoPlayback.StreamPlayback) {
            if (!video.playback.hasOriginalOrFHDResolution()) throw InsufficientVideoResolutionException(video.videoId)
        } else {
            throw VideoPlaybackNotFound("The requested video cannot be downloaded because it comes from an incompatible source")
        }
    }

    private fun getDefaultCaptions(video: Video): Caption {
        return try {
            val availableCaptions = captionService.getAvailableCaptions(video.videoId)
            availableCaptions.firstOrNull { it.default }
                ?: availableCaptions.firstOrNull()
                ?: throw VideoCaptionNotFound(videoId = video.videoId)
        } catch (e: UnsupportedCaptionsException) {
            throw VideoCaptionNotFound(videoId = video.videoId)
        }
    }

    fun writeCompressedContent(outputStream: OutputStream, video: Video, caption: Caption) {
        ZipOutputStream(outputStream).use {
            it.writeEntry(buildFilename(video.title).plus(".${caption.format.getFileExtension()}")) { os ->
                os.write(caption.content.toByteArray())
            }

            it.writeEntry(buildFilename(video.title).plus(".${playbackProvider.getExtensionForAsset(video.playback.id)}")) { os ->
                playbackProvider.downloadHighestResolutionVideo(video.playback.id, os)
            }

        }
    }

    private fun ZipOutputStream.writeEntry(filename: String, contentWriter: (os: OutputStream) -> Unit) {
        this.putNextEntry(ZipEntry(filename).apply { compressedSize = -1 })
        logger.info { "writing zip entry ${filename}" }
        contentWriter(this)
        this.closeEntry()
        logger.info { "closed zip entry ${filename}" }
    }
}
