package com.boclips.videos.service.application.video

import com.boclips.videos.service.application.video.exceptions.VideoCaptionNotFound
import com.boclips.videos.service.application.video.search.SearchVideo
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.model.video.Caption
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.UnsupportedCaptionsException
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.service.video.CaptionService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class GetVideoAssets(
    private val captionService: CaptionService,
    private val searchVideo: SearchVideo
) {

    companion object {
        fun buildFilename(title: String) =
            title
                .replace(Regex("[^A-Za-z\\s\\d]+"), "")
                .replace(Regex("[\\s]+"), "-")

        fun writeCompressedContent(outputStream: OutputStream, title: String, caption: Caption) {
            ZipOutputStream(outputStream).let { archive ->
                val subtitles = ZipEntry(buildFilename(title).plus(".${caption.format.getFileExtension()}"))
                archive.putNextEntry(subtitles)
                archive.write(caption.content.toByteArray())
                archive.closeEntry()
                archive.close()
            }
        }
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
            .body(StreamingResponseBody { writeCompressedContent(it, video.title, caption) })
    }
}
