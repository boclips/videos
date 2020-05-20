package com.boclips.contentpartner.service.presentation.channel

import com.boclips.contentpartner.service.application.channel.CreateChannel
import com.boclips.contentpartner.service.application.channel.GetChannel
import com.boclips.contentpartner.service.application.channel.GetChannels
import com.boclips.contentpartner.service.application.channel.UpdateChannel
import com.boclips.contentpartner.service.domain.model.SignedLinkProvider
import com.boclips.contentpartner.service.presentation.converters.ChannelToResourceConverter
import com.boclips.contentpartner.service.presentation.hateoas.LegacyContentPartnerLinkBuilder
import com.boclips.videos.api.request.SignedLinkRequest
import com.boclips.videos.api.request.channel.ChannelFilterRequest
import com.boclips.videos.api.request.channel.ChannelRequest
import com.boclips.videos.api.response.channel.ChannelResource
import com.boclips.videos.api.response.channel.ChannelWrapperResource
import com.boclips.videos.api.response.channel.ChannelsResource
import com.boclips.videos.service.domain.model.video.contentpartner.ContentPartnerId
import com.boclips.videos.service.domain.service.video.VideoRepository
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
@RequestMapping("/v1/channels")
class ChannelController(
    private val videoRepository: VideoRepository,
    private val createChannel: CreateChannel,
    private val updateChannel: UpdateChannel,
    private val fetchChannel: GetChannel,
    private val fetchChannels: GetChannels,
    private val legacyContentPartnerLinkBuilder: LegacyContentPartnerLinkBuilder,
    private val channelToResourceConverter: ChannelToResourceConverter,
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
    fun getChannels(channelFilterRequest: ChannelFilterRequest): ChannelsResource {
        val channels = fetchChannels(
            name = channelFilterRequest.name,
            official = channelFilterRequest.official,
            accreditedToYtChannelId = channelFilterRequest.accreditedToYtChannelId,
            ingestTypes = channelFilterRequest.ingestType
        )

        val resources = channels.map {
            channelToResourceConverter.convert(it)
        }

        return ChannelsResource(_embedded = ChannelWrapperResource(channels = resources))
    }

    @GetMapping("/{id}")
    fun getChannel(@PathVariable("id") @NotBlank channelId: String?): ResponseEntity<ChannelResource> {
        val channelResource = fetchChannel(channelId!!)
            .let { channelToResourceConverter.convert(it) }
            .copy(_links = listOf(legacyContentPartnerLinkBuilder.self(channelId)).map { it.rel to it }.toMap())

        return ResponseEntity(channelResource, HttpStatus.OK)
    }

    @PostMapping
    fun postChannel(@Valid @RequestBody upsertChannelRequest: ChannelRequest): ResponseEntity<Void> {
        val channel = createChannel(upsertChannelRequest)

        return ResponseEntity(HttpHeaders().apply {
            set(
                "Location",
                legacyContentPartnerLinkBuilder.self(channel.id.value).href
            )
        }, HttpStatus.CREATED)
    }

    @PatchMapping("/{id}")
    fun patchChannel(
        @PathVariable("id") channelId: String,
        @Valid @RequestBody updateChannelRequest: ChannelRequest
    ): ResponseEntity<Void> {
        updateChannel(channelId = channelId, upsertRequest = updateChannelRequest)
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
