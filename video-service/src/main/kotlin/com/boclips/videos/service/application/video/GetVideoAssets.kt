package com.boclips.videos.service.application.video

import com.boclips.videos.service.application.video.exceptions.VideoCaptionNotFound
import com.boclips.videos.service.application.video.search.SearchVideo
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.model.video.Caption
import com.boclips.videos.service.domain.model.video.UnsupportedCaptionsException
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.service.video.CaptionService
import com.boclips.videos.service.domain.service.video.plackback.PlaybackProvider
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

    companion object {
        fun buildFilename(title: String) =
            title
                .replace(Regex("[^A-Za-z\\s\\d]+"), "")
                .replace(Regex("[\\s]+"), "-")
    }

    operator fun invoke(videoId: String, user: User): ResponseEntity<StreamingResponseBody> {
        val video = searchVideo.byId(videoId, user)

        val caption = try {
            captionService.getAvailableCaptions(video.videoId)
                .firstOrNull()
                ?: throw VideoCaptionNotFound(videoId = video.videoId)
        } catch (e: UnsupportedCaptionsException) {
            throw VideoCaptionNotFound(videoId = video.videoId)
        }

        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=\"${buildFilename(video.title)}.zip\"")
            .contentType(MediaType("application", "zip"))
            .body(StreamingResponseBody { writeCompressedContent(it, video, caption) })
    }

    fun writeCompressedContent(outputStream: OutputStream, video: Video, caption: Caption) {
        ZipOutputStream(outputStream).use {
            it.writeEntry(ZipEntry(buildFilename(video.title).plus(".${caption.format.getFileExtension()}"))) { os ->
                os.write(caption.content.toByteArray())
            }

            it.writeEntry(ZipEntry(buildFilename(video.title).plus(".${playbackProvider.getExtensionForAsset(video.playback.id)}"))){ os ->
                playbackProvider.downloadFHDOrOriginalAsset(video.playback.id, os)
            }
        }
    }

    private fun ZipOutputStream.writeEntry(zipEntry: ZipEntry, contentWriter: (os: OutputStream) -> Unit) {
        this.putNextEntry(zipEntry)
        contentWriter(this)
        this.closeEntry()
    }
}
