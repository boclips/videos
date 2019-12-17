package com.boclips.videos.service.infrastructure.video.converters

import com.boclips.videos.service.domain.model.video.ContentPartner
import com.boclips.videos.service.domain.model.video.ContentPartnerId
import com.boclips.videos.service.infrastructure.video.ContentPartnerDocument
import org.bson.types.ObjectId

object ContentPartnerDocumentConverter {
    fun toContentPartnerDocument(contentPartner: ContentPartner): ContentPartnerDocument {
        return ContentPartnerDocument(
            id = ObjectId(contentPartner.contentPartnerId.value),
            name = contentPartner.name
        )
    }

    fun toContentPartner(document: ContentPartnerDocument): ContentPartner {
        return ContentPartner(
            contentPartnerId = ContentPartnerId(value = document.id.toString()),
            name = document.name
        )
    }
}
