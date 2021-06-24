package com.boclips.videos.service.presentation

import com.boclips.contentpartner.service.application.channel.BroadcastChannels
import com.boclips.contentpartner.service.application.contract.BroadcastContracts
import com.boclips.contentpartner.service.application.contract.legalrestrictions.BroadcastContractLegalRestrictions
import com.boclips.videos.api.response.video.VideoIdsResource
import com.boclips.videos.api.response.video.VideoIdsWrapper
import com.boclips.videos.service.application.collection.BroadcastCollections
import com.boclips.videos.service.application.exceptions.VideoNotAnalysableException
import com.boclips.videos.service.application.subject.SubjectClassificationService
import com.boclips.videos.service.application.video.BroadcastVideos
import com.boclips.videos.service.application.video.GetVideosByContentPackage
import com.boclips.videos.service.application.video.VideoAnalysisService
import com.boclips.videos.service.domain.service.GetUserIdOverride
import com.boclips.videos.service.domain.service.user.AccessRuleService
import com.boclips.videos.service.domain.service.user.UserService
import com.boclips.videos.service.domain.service.video.VideoDuplicationService
import com.boclips.videos.service.presentation.hateoas.AdminLinkBuilder
import mu.KLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
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
    private val videoDuplicationService: VideoDuplicationService,
    private val broadcastContractLegalRestrictions: BroadcastContractLegalRestrictions,
    private val getVideosByContentPackage: GetVideosByContentPackage,
    private val adminLinkBuilder: AdminLinkBuilder,
    getUserIdOverride: GetUserIdOverride,
    accessRuleService: AccessRuleService,
    userService: UserService,
) : BaseController(accessRuleService, getUserIdOverride, userService) {
    companion object : KLogging() {
        const val DEFAULT_PAGE_SIZE = 10000
        const val MAX_PAGE_SIZE = 10000
    }

    @PostMapping("/analyse_video/{videoId}")
    fun postAnalyseVideo(
        @PathVariable videoId: String,
        @RequestParam language: Locale?,
        @RequestParam retry: Boolean?
    ): ResponseEntity<Void> {
        try {
            videoAnalysisService.analysePlayableVideo(videoId, language = language, retry = retry)
        } catch (e: VideoNotAnalysableException) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }

        return ResponseEntity(HttpStatus.ACCEPTED)
    }

    @PostMapping("/analyse_videos")
    fun postAnalyseVideos(@RequestParam channelId: String, @RequestParam language: Locale?): ResponseEntity<Void> {
        try {
            videoAnalysisService.analyseVideosOfChannel(channelId, language = language)
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

    @PostMapping("/clean_deactivated_videos")
    fun cleanDeactivatedVideos(): ResponseEntity<Void> {
        videoDuplicationService.cleanAllDeactivatedVideos()
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

    @PostMapping("/broadcast_contract_legal_restrictions")
    fun issueBroadcastContractLegalRestrictions() {
        broadcastContractLegalRestrictions()
    }

    @GetMapping("/videos_for_content_package/{contentPackageId}")
    fun getVideosForContentPackage(
        @PathVariable("contentPackageId") contentPackageId: String,
        @RequestParam(name = "size", required = false) size: Int?,
        @RequestParam(name = "cursor", required = false) cursorId: String?,
    ): VideoIdsResource {
        val pageSize = size ?: DEFAULT_PAGE_SIZE
        validatePageSize(pageSize)
        val result = getVideosByContentPackage(
            contentPackageId = contentPackageId,
            pageSize = pageSize,
            cursorId = cursorId
        )
        return VideoIdsResource(
            _embedded = VideoIdsWrapper(result.videoIds.map { it.value }),
            _links = result.cursor?.let { cursor ->
                mapOf(
                    "next" to adminLinkBuilder.nextContentPackage(
                        contentPackageId = contentPackageId,
                        cursorId = cursor.value,
                        size = pageSize
                    )
                )
            }
        )
    }

    private fun validatePageSize(pageSize: Int) {
        if (pageSize > MAX_PAGE_SIZE) throw IllegalArgumentException()
        if (pageSize <= 0) throw IllegalArgumentException()
    }
}
