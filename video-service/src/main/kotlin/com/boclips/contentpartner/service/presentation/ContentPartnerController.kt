package com.boclips.contentpartner.service.presentation

import com.boclips.contentpartner.service.application.CreateContentPartner
import com.boclips.contentpartner.service.application.GetContentPartner
import com.boclips.contentpartner.service.application.GetContentPartners
import com.boclips.contentpartner.service.application.UpdateContentPartner
import com.boclips.contentpartner.service.domain.model.ContentPartnerId
import com.boclips.videos.service.domain.model.video.VideoRepository
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
import org.springframework.web.bind.annotation.RequestParam
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
) : BaseController() {
    @RequestMapping(
        "/{contentPartnerId}/videos/{contentPartnerVideoId}",
        method = [RequestMethod.HEAD]
    )
    fun lookupVideoByProviderId(
        @PathVariable("contentPartnerId") contentPartnerId: String,
        @PathVariable("contentPartnerVideoId") contentPartnerVideoId: String
    ): ResponseEntity<Void> {
        val exists = videoRepository.existsVideoFromContentPartnerId(
            ContentPartnerId(value = contentPartnerId),
            contentPartnerVideoId
        )

        val status = if (exists) HttpStatus.OK else HttpStatus.NOT_FOUND
        return ResponseEntity(status)
    }

    @PostMapping("/{contentPartnerId}/videos/search")
    fun postSearchVideoByProviderId(
        @PathVariable("contentPartnerId") contentPartnerId: String,
        @RequestBody contentPartnerVideoId: String
    ): ResponseEntity<Void> {
        val exists = videoRepository.existsVideoFromContentPartnerId(
            ContentPartnerId(value = contentPartnerId),
            contentPartnerVideoId
        )

        val status = if (exists) HttpStatus.OK else HttpStatus.NOT_FOUND
        return ResponseEntity(status)
    }

    @GetMapping
    fun getContentPartners(
        @RequestParam("name", required = false) name: String?,
        @RequestParam("official", required = false) official: Boolean?,
        @RequestParam("accreditedToYtChannelId", required = false) accreditedToYtChannelId: String?
    ): Resources<Resource<ContentPartnerResource>> {
        val user = getCurrentUser()
        return fetchContentPartners(user = user, name = name, official = official, accreditedToYtChannelId = accreditedToYtChannelId)
    }

    @GetMapping("/{id}")
    fun getContentPartner(@PathVariable("id") contentPartnerId: String?): Resource<ContentPartnerResource> {
        val user = getCurrentUser()
        return fetchContentPartner(contentPartnerId, user).let {
            Resource(it, contentPartnersLinkBuilder.self(it.id))
        }
    }

    @PostMapping
    fun postContentPartner(@Valid @RequestBody createContentPartnerRequest: ContentPartnerRequest): ResponseEntity<Void> {
        val contentPartner = createContentPartner(createContentPartnerRequest)

        return ResponseEntity(HttpHeaders().apply {
            set(
                "Location",
                contentPartnersLinkBuilder.self(contentPartner.contentPartnerId.value).href.toString()
            )
        }, HttpStatus.CREATED)
    }

    @PutMapping("/{id}")
    fun putContentPartner(
        @PathVariable("id") contentPartnerId: String,
        @Valid @RequestBody updateContentPartnerRequest: ContentPartnerRequest
    ): ResponseEntity<Void> {
        updateContentPartner(contentPartnerId = contentPartnerId, request = updateContentPartnerRequest)

        return ResponseEntity(HttpStatus.NO_CONTENT)
    }
}
