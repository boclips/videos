package com.boclips.videos.service.application.video

import com.boclips.videos.service.application.video.exceptions.VideoCaptionNotFound
import com.boclips.videos.service.application.video.search.SearchVideo
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.model.video.Caption
import com.boclips.videos.service.domain.model.video.UnsupportedCaptionsException
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.service.video.CaptionService
import org.springframework.http.ResponseEntity

class GetVideoAssets(
    private val captionService: CaptionService,
    private val searchVideo: SearchVideo
) {

    companion object {
        fun buildFilename(video: Video, caption: Caption) =
            video.title
                .replace(Regex("[^A-Za-z\\s\\d]+"), "")
                .replace(Regex("[\\s]+"), "-")
                .plus(".${caption.format.getFileExtension()}")
    }

    operator fun invoke(videoId: String, user: User): ResponseEntity<String> {
        val video = searchVideo.byId(videoId, user)

        val caption = try {
            captionService.getAvailableCaptions(video.videoId)
                .firstOrNull()
                ?: throw VideoCaptionNotFound(videoId = video.videoId)
        } catch (e: UnsupportedCaptionsException) {
            throw VideoCaptionNotFound(videoId = video.videoId)
        }

        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=\"${buildFilename(video, caption)}\"")
            .body(caption.content)
    }
}
