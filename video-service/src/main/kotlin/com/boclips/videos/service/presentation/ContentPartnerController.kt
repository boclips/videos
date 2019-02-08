package com.boclips.videos.service.presentation

import com.boclips.videos.service.domain.model.asset.VideoAssetRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/content-partners")
class ContentPartnerController(
    val videoAssetRepository: VideoAssetRepository
) {

    @RequestMapping(
        "/{contentPartnerId}/videos/{contentPartnerVideoId}",
        method = [RequestMethod.HEAD]
    )
    fun lookupVideoByProviderId(
        @PathVariable("contentPartnerId") contentPartnerId: String,
        @PathVariable("contentPartnerVideoId") contentPartnerVideoId: String
    ): ResponseEntity<Void> {

        val exists = videoAssetRepository.existsVideoFromContentPartner(contentPartnerId, contentPartnerVideoId)

        val status = if (exists) HttpStatus.OK else HttpStatus.NOT_FOUND
        return ResponseEntity(status)
    }
}

