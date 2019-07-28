package com.boclips.videos.service.presentation

import com.boclips.search.service.domain.common.ProgressNotifier
import com.boclips.videos.service.application.collection.RebuildCollectionIndex
import com.boclips.videos.service.application.exceptions.VideoNotAnalysableException
import com.boclips.videos.service.application.video.RebuildLegacySearchIndex
import com.boclips.videos.service.application.subject.SubjectClassificationService
import com.boclips.videos.service.application.video.DispatchVideoUpdatedEvents
import com.boclips.videos.service.application.video.RebuildVideoIndex
import com.boclips.videos.service.application.video.VideoPlaybackService
import com.boclips.videos.service.application.video.VideoAnalysisService
import mu.KLogging
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter
import java.util.Locale
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class ResponseEmitterProgressNotifier(private val emitter: ResponseBodyEmitter) :
    ProgressNotifier {
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
    private val rebuildVideoIndex: RebuildVideoIndex,
    private val rebuildCollectionIndex: RebuildCollectionIndex,
    private val rebuildLegacySearchIndex: RebuildLegacySearchIndex,
    private val videoPlaybackService: VideoPlaybackService,
    private val dispatchVideoUpdatedEvents: DispatchVideoUpdatedEvents,
    private val subjectClassificationService: SubjectClassificationService,
    private val videoAnalysisService: VideoAnalysisService
) {
    companion object : KLogging()

    @PostMapping("/rebuild_video_index")
    fun rebuildVideoIndex(): ResponseEntity<ResponseBodyEmitter> {
        return asyncWithNotifier(rebuildVideoIndex::invoke)
    }

    @PostMapping("/rebuild_collection_index")
    fun rebuildCollectionIndex(): ResponseEntity<ResponseBodyEmitter> {
        return asyncWithNotifier(rebuildCollectionIndex::invoke)
    }

    @PostMapping("/build_legacy_search_index")
    fun buildLegacySearchIndex(): ResponseEntity<ResponseBodyEmitter> {
        return asyncWithNotifier(rebuildLegacySearchIndex::invoke)
    }

    @PostMapping("/refresh_playbacks")
    fun refreshVideoDurations(@RequestParam source: String?): ResponseEntity<Void> {
        videoPlaybackService.requestUpdate(source)
        return ResponseEntity(HttpStatus.OK)
    }

    @PostMapping("/analyse_video/{videoId}")
    fun postAnalyseVideo(@PathVariable videoId: String, @RequestParam language: Locale?): ResponseEntity<Void> {
        try {
            videoAnalysisService.analysePlayableVideo(videoId, language = language)
        } catch (e: VideoNotAnalysableException) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }

        return ResponseEntity(HttpStatus.ACCEPTED)
    }

    @PostMapping("/analyse_videos")
    fun postAnalyseVideos(@RequestParam contentPartner: String, @RequestParam language: Locale?): ResponseEntity<Void> {
        try {
            videoAnalysisService.analyseVideosOfContentPartner(contentPartner, language = language)
        } catch (e: VideoNotAnalysableException) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        return ResponseEntity(HttpStatus.ACCEPTED)
    }

    @PostMapping("/classify_videos")
    fun postClassifyVideos(@RequestParam contentPartner: String?): ResponseEntity<Void> {
        subjectClassificationService.classifyVideosByContentPartner(contentPartner)
        return ResponseEntity(HttpStatus.ACCEPTED)
    }

    @PostMapping("/dispatch_video_updated_events")
    fun postDispatchVideoUpdatedEvents() {
        dispatchVideoUpdatedEvents()
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
