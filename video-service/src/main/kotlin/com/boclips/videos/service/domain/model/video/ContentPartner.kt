package com.boclips.videos.service.domain.model.video

import com.boclips.contentpartner.service.domain.model.ContentPartnerId

data class ContentPartner(
    val contentPartnerId: ContentPartnerId,
    val name: String
) {
    override fun toString(): String {
        return "ContentPartner(id = ${this.contentPartnerId.value}, name = ${this.name})"
    }
}
