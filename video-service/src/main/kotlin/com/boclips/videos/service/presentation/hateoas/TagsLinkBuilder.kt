package com.boclips.videos.service.presentation.hateoas

import com.boclips.security.utils.UserExtractor.getIfHasRole
import com.boclips.videos.service.config.security.UserRoles
import org.springframework.hateoas.Link
import org.springframework.stereotype.Component

@Component
class TagsLinkBuilder(private val uriComponentsBuilderFactory: UriComponentsBuilderFactory) {

    fun tags(rel: String = "tags") = getIfHasRole(UserRoles.VIEW_TAGS) {
        Link(getTagRoot().toUriString(), rel)
    }

    fun tag(rel: String = "self", id: String): Link? {
        return getIfHasRole(UserRoles.VIEW_TAGS) {
            Link(getTagRoot().pathSegment(id).toUriString(), rel)
        }
    }

    private fun getTagRoot() = uriComponentsBuilderFactory.getInstance()
        .replacePath("/v1/tags")
        .replaceQueryParams(null)
}
