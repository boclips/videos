package com.boclips.videos.service.infrastructure.contentPartner

import com.boclips.videos.service.domain.model.ageRange.AgeRange
import com.boclips.videos.service.domain.model.contentPartner.ContentPartner
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerId
import org.bson.types.ObjectId

object ContentPartnerDocumentConverter {
    fun toContentPartnerDocument(contentPartner: ContentPartner): ContentPartnerDocument {
        return ContentPartnerDocument(
            id = ObjectId(contentPartner.contentPartnerId.value),
            name = contentPartner.name,
            ageRangeMax = contentPartner.ageRange.max(),
            ageRangeMin = contentPartner.ageRange.min()
        )
    }

    fun toContentPartner(document: ContentPartnerDocument): ContentPartner {
        return ContentPartner(
            contentPartnerId = ContentPartnerId(document.id.toString()),
            name = document.name,
            ageRange = if (document.ageRangeMin !== null) AgeRange.bounded(
                document.ageRangeMin,
                document.ageRangeMax
            ) else AgeRange.unbounded()
        )
    }
}