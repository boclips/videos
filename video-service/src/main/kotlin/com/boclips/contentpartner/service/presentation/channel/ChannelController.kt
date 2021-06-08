package com.boclips.contentpartner.service.presentation.channel

import com.boclips.contentpartner.service.application.channel.CreateChannel
import com.boclips.contentpartner.service.application.channel.GetChannel
import com.boclips.contentpartner.service.application.channel.GetChannels
import com.boclips.contentpartner.service.application.channel.UpdateChannel
import com.boclips.contentpartner.service.domain.model.SignedLinkProvider
import com.boclips.contentpartner.service.domain.model.channel.ChannelSortKey
import com.boclips.contentpartner.service.presentation.converters.ChannelToResourceConverter
import com.boclips.contentpartner.service.presentation.converters.IngestTypeConverter
import com.boclips.contentpartner.service.presentation.hateoas.ChannelLinkBuilder
import com.boclips.videos.api.request.Projection
import com.boclips.videos.api.request.SignedLinkRequest
import com.boclips.videos.api.request.channel.ChannelFilterRequest
import com.boclips.videos.api.request.channel.ChannelRequest
import com.boclips.videos.api.request.channel.SortByRequest
import com.boclips.videos.api.response.channel.ChannelResource
import com.boclips.videos.api.response.channel.ChannelsResource
import com.boclips.videos.service.domain.model.video.channel.ChannelId
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
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid
import javax.validation.constraints.NotBlank

@RestController
@RequestMapping("/v1/channels")
class ChannelController(
    private val videoRepository: VideoRepository,
    private val createChannel: CreateChannel,
    private val updateChannel: UpdateChannel,
    private val getChannel: GetChannel,
    private val getChannels: GetChannels,
    private val channelLinkBuilder: ChannelLinkBuilder,
    private val channelToResourceConverter: ChannelToResourceConverter,
    private val marketingSignedLinkProvider: SignedLinkProvider
) {
    @PostMapping("/{channelId}/videos/search")
    fun postSearchVideoByProviderId(
        @PathVariable("channelId") channelId: String,
        @RequestBody channelVideoId: String
    ): ResponseEntity<Void> {
        val exists = videoRepository.existsVideoFromChannelId(
            ChannelId(value = channelId),
            channelVideoId
        )

        val status = if (exists) HttpStatus.OK else HttpStatus.NOT_FOUND
        return ResponseEntity(status)
    }

    @RequestMapping(
        path = ["/{channelId}/videos/{channelVideoId}"],
        method = [RequestMethod.HEAD]
    )
    fun getVideoByProviderId(
        @PathVariable("channelId") channelId: String,
        @PathVariable("channelVideoId") channelVideoId: String
    ): ResponseEntity<Void> {
        val exists = videoRepository.existsVideoFromChannelId(
            ChannelId(value = channelId),
            channelVideoId
        )

        val status = if (exists) HttpStatus.OK else HttpStatus.NOT_FOUND
        return ResponseEntity(status)
    }

    @GetMapping
    fun channels(channelFilterRequest: ChannelFilterRequest): ResponseEntity<ChannelsResource> {

        val channelsPageResult = getChannels(
            name = channelFilterRequest.name,
            ingestTypes = channelFilterRequest.ingestType?.map {
                IngestTypeConverter.convertType(it)
            },
            categories = channelFilterRequest.categories,
            sortBy = when (channelFilterRequest.sort_by) {
                SortByRequest.CATEGORIES_ASC -> ChannelSortKey.CATEGORIES_ASC
                SortByRequest.CATEGORIES_DESC -> ChannelSortKey.CATEGORIES_DESC
                SortByRequest.NAME_ASC -> ChannelSortKey.NAME_ASC
                SortByRequest.NAME_DESC -> ChannelSortKey.NAME_DESC
                SortByRequest.YOUTUBE_ASC -> ChannelSortKey.YOUTUBE_ASC
                SortByRequest.YOUTUBE_DESC -> ChannelSortKey.YOUTUBE_DESC
                null -> null
            },
            size = channelFilterRequest.size,
            page = channelFilterRequest.page
        )

        val channelsResource = channelToResourceConverter.convert(channelsPageResult, channelFilterRequest.projection)

        return ResponseEntity(channelsResource, HttpStatus.OK)
    }

    @GetMapping("/{id}")
    fun channel(@PathVariable("id") @NotBlank channelId: String?): ResponseEntity<ChannelResource> {
        val channelResource = getChannel(channelId!!)
            .let { channelToResourceConverter.convert(it, Projection.details) }
            .copy(_links = listOf(channelLinkBuilder.self(channelId)).map { it.rel.value() to it }.toMap())

        return ResponseEntity(channelResource, HttpStatus.OK)
    }

    @PostMapping
    fun postChannel(@Valid @RequestBody upsertChannelRequest: ChannelRequest): ResponseEntity<Void> {
        val channel = createChannel(upsertChannelRequest)

        return ResponseEntity(
            HttpHeaders().apply {
                set(
                    "Location",
                    channelLinkBuilder.self(channel.id.value).href
                )
            },
            HttpStatus.CREATED
        )
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
        return ResponseEntity(
            HttpHeaders().apply {
                set(
                    "Location",
                    link.toString()
                )
            },
            HttpStatus.NO_CONTENT
        )
    }
}
