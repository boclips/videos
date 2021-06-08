package com.boclips.contentpartner.service.presentation.hateoas

import com.boclips.security.utils.UserExtractor.getIfHasAnyRole
import com.boclips.security.utils.UserExtractor.getIfHasRole
import com.boclips.videos.service.config.security.UserRoles
import org.springframework.hateoas.Link

class ChannelLinkBuilder(private val uriComponentsBuilderFactory: UriComponentsBuilderFactory) {
    object Rels {
        const val CHANNELS = "channels"
        const val CHANNEL = "channel"
    }

    private fun hasId(id: String?): String {
        return when (id) {
            null -> "/{id}"
            else -> "/$id"
        }
    }

    fun self(id: String): Link {
        return Link.of(
            getChannelsRoot()
                .build()
                .toUriString()
                .plus(hasId(id)),
            "self"
        )
    }

    fun channelLink(id: String?): Link? {
        return getIfHasRole(UserRoles.VIEW_CHANNELS) {
            Link.of(
                getChannelsRoot()
                    .build()
                    .toUriString()
                    .plus(hasId(id)),
                Rels.CHANNEL
            )
        }
    }

    fun channelsLink(): Link? {
        return getIfHasRole(UserRoles.VIEW_CHANNELS) {
            Link.of(
                getChannelsRoot()
                    .build()
                    .toUriString()
                    .plus("{?name,projection,sort_by,page,size,categories,ingestType*}"),
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
