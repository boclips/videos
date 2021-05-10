package com.boclips.contentpartner.service.presentation.hateoas

import com.boclips.contentpartner.service.presentation.channel.ChannelController
import com.boclips.security.utils.UserExtractor.getIfHasAnyRole
import com.boclips.security.utils.UserExtractor.getIfHasRole
import com.boclips.videos.api.response.HateoasLink
import com.boclips.videos.service.config.security.UserRoles
import org.springframework.hateoas.Link
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder

class ChannelLinkBuilder(private val uriComponentsBuilderFactory: UriComponentsBuilderFactory) {
    object Rels {
        const val CHANNELS = "channels"
    }

    fun self(id: String): HateoasLink {
        val withSelfRel = WebMvcLinkBuilder.linkTo(
            WebMvcLinkBuilder.methodOn(ChannelController::class.java).getChannel(
                id
            )
        ).withSelfRel()

        return HateoasLink(href = withSelfRel.href, rel = withSelfRel.rel.value())
    }

    fun channelLink(id: String?): Link? {
        return getIfHasRole(UserRoles.VIEW_CHANNELS) {
            WebMvcLinkBuilder.linkTo(
                WebMvcLinkBuilder.methodOn(ChannelController::class.java).getChannel(
                    id
                )
            ).withRel("channel")
        }
    }

    fun channelsLink(): Link? {
        return getIfHasRole(UserRoles.VIEW_CHANNELS) {
            Link.of(
                getChannelsRoot()
                    .build()
                    .toUriString()
                    .plus("{?name,projection,ingestType}"),
                Rels.CHANNELS
            )
        }
    }

    fun channelsSignedUploadLink(): Link? =
        getIfHasAnyRole(UserRoles.INSERT_CHANNELS, UserRoles.UPDATE_CHANNELS) {
            Link.of(
                getChannelsRoot()
                    .build()
                    .toUriString()
                    .plus("/signed-upload-link"),
                "channelsSignedUploadLink"
            )
        }

    private fun getChannelsRoot() = uriComponentsBuilderFactory.getInstance()
        .replacePath("/v1/channels")
        .replaceQueryParams(null)
}
