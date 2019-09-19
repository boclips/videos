package com.boclips.videos.service.domain.model.video

import com.boclips.contentpartner.service.domain.model.ContentPartnerId
import com.boclips.videos.service.domain.model.common.AgeRange
import com.boclips.videos.service.domain.model.legal.restrictions.LegalRestrictions

data class ContentPartner(
    val contentPartnerId: ContentPartnerId,
    val name: String,
    val ageRange: AgeRange,
    val legalRestrictions: LegalRestrictions?
) {
    override fun toString(): String {
        return "ContentPartner(id = ${this.contentPartnerId.value}, name = ${this.name})"
    }
}
