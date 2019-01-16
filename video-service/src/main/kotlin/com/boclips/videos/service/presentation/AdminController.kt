package com.boclips.videos.service.presentation

import com.boclips.search.service.domain.ProgressNotifier
import com.boclips.videos.service.application.video.BuildLegacySearchIndex
import com.boclips.videos.service.application.video.RebuildSearchIndex
import mu.KLogging
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter
import java.util.concurrent.CompletableFuture


class ResponseEmitterProgressNotifier(private val emitter: ResponseBodyEmitter) : ProgressNotifier {
    override fun send(message: String) {
        emitter.send(message.trimEnd() + "\n", MediaType.TEXT_PLAIN)
    }
}

@RestController
@RequestMapping("/v1/admin/actions")
class AdminController(
        private val rebuildSearchIndex: RebuildSearchIndex,
        private val buildLegacySearchIndex: BuildLegacySearchIndex
) {

    companion object : KLogging()

    @PostMapping("/rebuild_search_index")
    fun rebuildSearchIndex(): ResponseEntity<ResponseBodyEmitter> {
        return asyncWithNotifier(rebuildSearchIndex::execute)
    }

    @PostMapping("/build_legacy_search_index")
    fun buildLegacySearchIndex(): ResponseEntity<ResponseBodyEmitter> {
        return asyncWithNotifier(buildLegacySearchIndex::execute)
    }

    private fun asyncWithNotifier(handler: (ResponseEmitterProgressNotifier) -> CompletableFuture<Unit>): ResponseEntity<ResponseBodyEmitter> {
        val emitter = ResponseBodyEmitter()
        handler(ResponseEmitterProgressNotifier(emitter))
                .thenApply {
                    emitter.complete()
                }.exceptionally { ex ->
                    emitter.completeWithError(ex)
                }

        return ResponseEntity(emitter, HttpStatus.OK)
    }
}