package com.boclips.contentpartner.service.domain.model

import com.boclips.videos.service.domain.model.common.AgeRange
import com.boclips.videos.service.domain.model.legal.restrictions.LegalRestrictions
import com.boclips.videos.service.domain.model.video.DistributionMethod

data class ContentPartner(
    val contentPartnerId: ContentPartnerId,
    val name: String,
    val ageRange: AgeRange,
    val credit: Credit,
    val legalRestrictions: LegalRestrictions?,
    val distributionMethods: Set<DistributionMethod>
) {
    fun isStreamable(): Boolean {
        return distributionMethods.contains(DistributionMethod.STREAM)
    }

    fun isDownloadable(): Boolean {
        return distributionMethods.contains(DistributionMethod.DOWNLOAD)
    }

    override fun toString(): String {
        return "ContentPartner(id = ${this.contentPartnerId.value}, name = ${this.name})"
    }
}
