package com.boclips.videos.service.presentation

import com.boclips.search.service.domain.ProgressNotifier
import com.boclips.videos.service.application.video.AnalyseVideo
import com.boclips.videos.service.application.video.BuildLegacySearchIndex
import com.boclips.videos.service.application.video.RebuildSearchIndex
import com.boclips.videos.service.application.video.RefreshVideoDurations
import com.boclips.videos.service.domain.exceptions.VideoNotAnalysableException
import mu.KLogging
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class ResponseEmitterProgressNotifier(private val emitter: ResponseBodyEmitter) : ProgressNotifier {
    companion object : KLogging()

    override fun send(message: String) {
        try {
            emitter.send(message.trimEnd() + "\n", MediaType.TEXT_PLAIN)
        } catch (e: IllegalStateException) {
            logger.warn("Unable to update progress - ResponseBodyEmitter closed - ignoring")
        }
    }
}

@RestController
@RequestMapping("/v1/admin/actions")
class AdminController(
    private val rebuildSearchIndex: RebuildSearchIndex,
    private val buildLegacySearchIndex: BuildLegacySearchIndex,
    private val refreshVideoDurations: RefreshVideoDurations,
    private val analyseVideo: AnalyseVideo
) {
    companion object : KLogging()

    @PostMapping("/rebuild_search_index")
    fun rebuildSearchIndex(): ResponseEntity<ResponseBodyEmitter> {
        return asyncWithNotifier(rebuildSearchIndex::invoke)
    }

    @PostMapping("/build_legacy_search_index")
    fun buildLegacySearchIndex(): ResponseEntity<ResponseBodyEmitter> {
        return asyncWithNotifier(buildLegacySearchIndex::invoke)
    }

    @PostMapping("/refresh_video_durations")
    fun refreshVideoDurations(): ResponseEntity<ResponseBodyEmitter> {
        return asyncWithNotifier(refreshVideoDurations::invoke)
    }

    @PostMapping("/analyse_video/{videoId}")
    fun postAnalyseVideo(@PathVariable videoId: String): ResponseEntity<Void> {
        try {
            analyseVideo(videoId)
        }
        catch (e: VideoNotAnalysableException) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }

        return ResponseEntity(HttpStatus.ACCEPTED)
    }

    private fun asyncWithNotifier(handler: (ResponseEmitterProgressNotifier) -> CompletableFuture<Unit>): ResponseEntity<ResponseBodyEmitter> {
        val emitter = ResponseBodyEmitter(TimeUnit.HOURS.toMillis(2))

        emitter.onTimeout {
            logger.warn("ResponseBodyEmitter timed out")
        }

        handler(ResponseEmitterProgressNotifier(emitter))
            .whenComplete { _, ex ->
                if (ex != null) {
                    emitter.completeWithError(ex)
                } else {
                    emitter.complete()
                }
            }

        return ResponseEntity(emitter, HttpStatus.OK)
    }
}