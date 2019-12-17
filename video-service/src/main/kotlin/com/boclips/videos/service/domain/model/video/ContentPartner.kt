package com.boclips.videos.service.domain.model.video

data class ContentPartner(
    val contentPartnerId: ContentPartnerId,
    val name: String
) {
    override fun toString(): String {
        return "ContentPartner(id = ${this.contentPartnerId.value}, name = ${this.name})"
    }
}
