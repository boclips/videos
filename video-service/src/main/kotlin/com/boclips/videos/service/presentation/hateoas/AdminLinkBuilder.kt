package com.boclips.videos.service.presentation.hateoas

import com.boclips.videos.api.response.HateoasLink
import org.springframework.hateoas.Link
import org.springframework.stereotype.Component

@Component
class AdminLinkBuilder(private val uriComponentsBuilderFactory: UriComponentsBuilderFactory) {
    fun nextContentPackage(
        contentPackageId: String,
        cursorId: String,
        size: Int
    ): HateoasLink =
        HateoasLink.of(
            Link.of(
                root()
                    .pathSegment("videos_for_content_package")
                    .pathSegment(contentPackageId)
                    .queryParam("size", size)
                    .queryParam("cursor", cursorId)
                    .toUriString(),
                "next"
            )
        )

    private fun root() = uriComponentsBuilderFactory.getInstance()
        .replacePath("/v1/admin/actions")
        .replaceQueryParams(null)
}
