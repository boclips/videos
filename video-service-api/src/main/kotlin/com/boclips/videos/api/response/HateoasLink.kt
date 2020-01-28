package com.boclips.videos.api.response

import org.springframework.hateoas.Link

data class HateoasLink(val href: String, val rel: String) {
    var templated: Boolean = href.contains(Regex("\\{.*\\}"))

    companion object {
        fun of(link: Link): HateoasLink {
            return HateoasLink(href = link.href, rel = link.rel.value())
        }
    }
}