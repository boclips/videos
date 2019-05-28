package com.boclips.videos.service.presentation

import com.boclips.videos.service.application.contentPartner.CreateContentPartner
import com.boclips.videos.service.application.contentPartner.UpdateContentPartner
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.presentation.contentPartner.ContentPartnerRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping("/v1/content-partners")
class ContentPartnerController(
    private val videoRepository: VideoRepository,
    private val createContentPartner: CreateContentPartner,
    private val updateContentPartner: UpdateContentPartner
) {

    @RequestMapping(
        "/{contentPartnerId}/videos/{contentPartnerVideoId}",
        method = [RequestMethod.HEAD]
    )
    fun lookupVideoByProviderId(
        @PathVariable("contentPartnerId") contentPartnerId: String,
        @PathVariable("contentPartnerVideoId") contentPartnerVideoId: String
    ): ResponseEntity<Void> {
        val exists = videoRepository.existsVideoFromContentPartner(contentPartnerId, contentPartnerVideoId)

        val status = if (exists) HttpStatus.OK else HttpStatus.NOT_FOUND
        return ResponseEntity(status)
    }

    @PostMapping
    fun createAContentPartner(@Valid @RequestBody createContentPartnerRequest: ContentPartnerRequest) : ResponseEntity<Void> {
        createContentPartner(createContentPartnerRequest)

        return ResponseEntity(HttpStatus.CREATED)
    }

    @PutMapping("/{contentPartnerId}")
    fun updateAContentPartner(
        @PathVariable("contentPartnerId") contentPartnerId: String,
        @Valid @RequestBody updateContentPartnerRequest: ContentPartnerRequest
    ) : ResponseEntity<Void> {
        updateContentPartner(existingContentPartnerName = contentPartnerId, request = updateContentPartnerRequest)

        return ResponseEntity(HttpStatus.CREATED)
    }
}

