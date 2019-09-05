package com.boclips.videos.service.domain.model.contentPartner

import com.boclips.videos.service.domain.model.common.AgeRange
import com.boclips.videos.service.domain.model.video.DistributionMethod

data class ContentPartner(
    val contentPartnerId: ContentPartnerId,
    val name: String,
    val ageRange: AgeRange,
    val credit: Credit,
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
