package com.boclips.contentpartner.service.presentation.contentpartner

import com.boclips.contentpartner.service.application.channel.CreateChannel
import com.boclips.contentpartner.service.application.channel.GetChannel
import com.boclips.contentpartner.service.application.channel.GetChannels
import com.boclips.contentpartner.service.application.channel.UpdateChannel
import com.boclips.contentpartner.service.domain.model.SignedLinkProvider
import com.boclips.contentpartner.service.presentation.converters.ContentPartnerToResourceConverter
import com.boclips.contentpartner.service.presentation.hateoas.ContentPartnersLinkBuilder
import com.boclips.videos.api.request.SignedLinkRequest
import com.boclips.videos.api.request.contentpartner.ContentPartnerFilterRequest
import com.boclips.videos.api.request.contentpartner.ContentPartnerRequest
import com.boclips.videos.api.response.contentpartner.ContentPartnerResource
import com.boclips.videos.api.response.contentpartner.ContentPartnerWrapperResource
import com.boclips.videos.api.response.contentpartner.ContentPartnersResource
import com.boclips.videos.service.domain.service.video.VideoRepository
import com.boclips.videos.service.domain.model.video.contentpartner.ContentPartnerId
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid
import javax.validation.constraints.NotBlank

@RestController
@RequestMapping("/v1/content-partners")
class ContentPartnerController(
    private val videoRepository: VideoRepository,
    private val createChannel: CreateChannel,
    private val updateChannel: UpdateChannel,
    private val fetchChannel: GetChannel,
    private val fetchChannels: GetChannels,
    private val contentPartnersLinkBuilder: ContentPartnersLinkBuilder,
    private val contentPartnerToResourceConverter: ContentPartnerToResourceConverter,
    private val marketingSignedLinkProvider: SignedLinkProvider
) {
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
    fun getContentPartners(contentPartnerFilterRequest: ContentPartnerFilterRequest): ContentPartnersResource {
        val contentPartners = fetchChannels(
            name = contentPartnerFilterRequest.name,
            official = contentPartnerFilterRequest.official,
            accreditedToYtChannelId = contentPartnerFilterRequest.accreditedToYtChannelId,
            ingestTypes = contentPartnerFilterRequest.ingestType
        )

        val resources = contentPartners.map {
            contentPartnerToResourceConverter.convert(it)
        }

        return ContentPartnersResource(_embedded = ContentPartnerWrapperResource(contentPartners = resources))
    }

    @GetMapping("/{id}")
    fun getContentPartner(@PathVariable("id") @NotBlank contentPartnerId: String?): ResponseEntity<ContentPartnerResource> {
        val contentPartnerResource = fetchChannel(contentPartnerId!!)
            .let { contentPartnerToResourceConverter.convert(it) }
            .copy(_links = listOf(contentPartnersLinkBuilder.self(contentPartnerId)).map { it.rel to it }.toMap())

        return ResponseEntity(contentPartnerResource, HttpStatus.OK)
    }

    @PostMapping
    fun postContentPartner(@Valid @RequestBody upsertContentPartnerRequest: ContentPartnerRequest): ResponseEntity<Void> {
        val contentPartner = createChannel(upsertContentPartnerRequest)

        return ResponseEntity(HttpHeaders().apply {
            set(
                "Location",
                contentPartnersLinkBuilder.self(contentPartner.id.value).href
            )
        }, HttpStatus.CREATED)
    }

    @PatchMapping("/{id}")
    fun patchContentPartner(
        @PathVariable("id") contentPartnerId: String,
        @Valid @RequestBody updateUpsertContentPartnerRequest: ContentPartnerRequest
    ): ResponseEntity<Void> {
        updateChannel(channelId = contentPartnerId, upsertRequest = updateUpsertContentPartnerRequest)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

    @PostMapping("/signed-upload-link")
    fun signedUploadLink(
        @RequestBody signedLinkRequest: SignedLinkRequest
    ): ResponseEntity<Void> {
        val link = marketingSignedLinkProvider.signedPutLink(signedLinkRequest.filename)
        return ResponseEntity(HttpHeaders().apply {
            set(
                "Location",
                link.toString()
            )
        }, HttpStatus.NO_CONTENT)
    }
}
