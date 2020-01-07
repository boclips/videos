package com.boclips.contentpartner.service.presentation

import com.boclips.contentpartner.service.application.CreateContentPartner
import com.boclips.contentpartner.service.application.GetContentPartner
import com.boclips.contentpartner.service.application.GetContentPartners
import com.boclips.contentpartner.service.application.UpdateContentPartner
import com.boclips.videos.api.request.contentpartner.CreateContentPartnerRequest
import com.boclips.videos.api.response.contentpartner.ContentPartnerResource
import com.boclips.videos.api.response.contentpartner.ContentPartnerWrapperResource
import com.boclips.videos.api.response.contentpartner.ContentPartnersResource
import com.boclips.videos.service.domain.model.video.ContentPartnerId
import com.boclips.videos.service.domain.model.video.VideoRepository
import org.springframework.hateoas.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid
import javax.validation.constraints.NotBlank

@RestController
@RequestMapping("/v1/content-partners")
class ContentPartnerController(
    private val videoRepository: VideoRepository,
    private val createContentPartner: CreateContentPartner,
    private val updateContentPartner: UpdateContentPartner,
    private val fetchContentPartner: GetContentPartner,
    private val fetchContentPartners: GetContentPartners,
    private val contentPartnersLinkBuilder: ContentPartnersLinkBuilder,
    private val contentPartnerToResourceConverter: ContentPartnerToResourceConverter
) : BaseController() {
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
    ): ContentPartnersResource {
        val user = getCurrentUser()
        val contentPartners = fetchContentPartners(
            name = name,
            official = official,
            accreditedToYtChannelId = accreditedToYtChannelId
        )

        val resources = contentPartners.map {
            contentPartnerToResourceConverter.convert(it, user)
        }

        return ContentPartnersResource(_embedded = ContentPartnerWrapperResource(contentPartners = resources))
    }

    @GetMapping("/{id}")
    fun getContentPartner(@PathVariable("id") @NotBlank contentPartnerId: String?): Resource<ContentPartnerResource> {
        val user = getCurrentUser()
        return fetchContentPartner(contentPartnerId!!, user).let {
            Resource(it, contentPartnersLinkBuilder.self(it.id))
        }
    }

    @PostMapping
    fun postContentPartner(@Valid @RequestBody createCreateContentPartnerRequest: CreateContentPartnerRequest): ResponseEntity<Void> {
        val contentPartner = createContentPartner(createCreateContentPartnerRequest)

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
        @Valid @RequestBody updateCreateContentPartnerRequest: CreateContentPartnerRequest
    ): ResponseEntity<Void> {
        updateContentPartner(contentPartnerId = contentPartnerId, createRequest = updateCreateContentPartnerRequest)

        return ResponseEntity(HttpStatus.NO_CONTENT)
    }
}
