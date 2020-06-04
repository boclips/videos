package com.boclips.videos.service.presentation

import com.boclips.contentpartner.service.application.channel.BroadcastChannels
import com.boclips.contentpartner.service.application.contentpartnercontract.BroadcastContracts
import com.boclips.videos.service.application.collection.BroadcastCollections
import com.boclips.videos.service.application.exceptions.VideoNotAnalysableException
import com.boclips.videos.service.application.subject.SubjectClassificationService
import com.boclips.videos.service.application.video.BroadcastVideos
import com.boclips.videos.service.application.video.VideoAnalysisService
import com.boclips.videos.service.domain.service.GetUserIdOverride
import com.boclips.videos.service.domain.service.user.AccessRuleService
import mu.KLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.Locale

@RestController
@RequestMapping("/v1/admin/actions")
class AdminController(
    private val broadcastVideos: BroadcastVideos,
    private val broadcastCollections: BroadcastCollections,
    private val broadcastChannels: BroadcastChannels,
    private val broadcastContracts: BroadcastContracts,
    private val subjectClassificationService: SubjectClassificationService,
    private val videoAnalysisService: VideoAnalysisService,
    getUserIdOverride: GetUserIdOverride,
    accessRuleService: AccessRuleService
) : BaseController(accessRuleService, getUserIdOverride) {
    companion object : KLogging()

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

    @PostMapping("/broadcast_videos")
    fun issueBroadcastVideos() {
        broadcastVideos()
    }

    @PostMapping("/broadcast_collections")
    fun issueBroadcastCollections() {
        broadcastCollections()
    }

    @PostMapping("/broadcast_channels")
    fun issueBroadcastChannels() {
        broadcastChannels()
    }

    @PostMapping("/broadcast_contracts")
    fun issueBroadcastContracts() {
        broadcastContracts()
    }
}
