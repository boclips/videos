package com.boclips.videos.service.presentation

import com.boclips.videos.service.application.contentPartner.CreateContentPartner
import com.boclips.videos.service.application.contentPartner.GetContentPartner
import com.boclips.videos.service.application.contentPartner.GetContentPartners
import com.boclips.videos.service.application.contentPartner.UpdateContentPartner
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.presentation.contentPartner.ContentPartnerRequest
import com.boclips.videos.service.presentation.contentPartner.ContentPartnerResource
import com.boclips.videos.service.presentation.hateoas.ContentPartnersLinkBuilder
import org.springframework.hateoas.Resource
import org.springframework.hateoas.Resources
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
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
    private val updateContentPartner: UpdateContentPartner,
    private val fetchContentPartner: GetContentPartner,
    private val fetchContentPartners: GetContentPartners,
    private val contentPartnersLinkBuilder: ContentPartnersLinkBuilder
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

    @GetMapping
    fun getContentPartners(): Resources<Resource<ContentPartnerResource>> {
        return fetchContentPartners()
    }

    @GetMapping("/{id}")
    fun getContentPartner(@PathVariable("id") contentPartnerId: String?): ContentPartnerResource {
        return fetchContentPartner(contentPartnerId)
    }

    @PostMapping
    fun postContentPartner(@Valid @RequestBody createContentPartnerRequest: ContentPartnerRequest): ResponseEntity<Void> {
        val contentPartner = createContentPartner(createContentPartnerRequest)

        return ResponseEntity(HttpHeaders().apply {
            set(
                "Location",
                contentPartnersLinkBuilder.self(contentPartner).href.toString()
            )
        }, HttpStatus.CREATED)
    }

    @PutMapping("/{contentPartnerId}")
    fun putContentPartner(
        @PathVariable("contentPartnerId") contentPartnerId: String,
        @Valid @RequestBody updateContentPartnerRequest: ContentPartnerRequest
    ): ResponseEntity<Void> {
        updateContentPartner(existingContentPartnerId = contentPartnerId, request = updateContentPartnerRequest)

        return ResponseEntity(HttpStatus.NO_CONTENT)
    }
}
