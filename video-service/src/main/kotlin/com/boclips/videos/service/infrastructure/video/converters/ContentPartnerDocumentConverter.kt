package com.boclips.videos.service.infrastructure.video.converters

import com.boclips.contentpartner.service.domain.model.ContentPartnerId
import com.boclips.videos.service.domain.model.common.AgeRange
import com.boclips.videos.service.domain.model.video.ContentPartner
import com.boclips.videos.service.infrastructure.legal.restrictions.LegalRestrictionsDocument
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.time.Instant

data class ContentPartnerDocument(
    @BsonId val id: ObjectId,
    val name: String,
    val ageRangeMin: Int?,
    val ageRangeMax: Int?,
    val lastModified: Instant? = null,
    val createdAt: Instant? = null,
    val legalRestrictions: LegalRestrictionsDocument? = null
)

object ContentPartnerDocumentConverter {
    fun toContentPartnerDocument(contentPartner: ContentPartner): ContentPartnerDocument {
        return ContentPartnerDocument(
            id = ObjectId(contentPartner.contentPartnerId.value),
            name = contentPartner.name,
            ageRangeMax = contentPartner.ageRange.max(),
            ageRangeMin = contentPartner.ageRange.min(),
            legalRestrictions = contentPartner.legalRestrictions?.let { LegalRestrictionsDocument.from(it) }
        )
    }

    fun toContentPartner(document: ContentPartnerDocument): ContentPartner {
        return ContentPartner(
            contentPartnerId = ContentPartnerId(value = document.id.toString()),
            name = document.name,
            ageRange = if (document.ageRangeMin !== null) AgeRange.bounded(
                document.ageRangeMin,
                document.ageRangeMax
            ) else AgeRange.unbounded(),
            legalRestrictions = document.legalRestrictions?.toRestrictions()
        )
    }
}
