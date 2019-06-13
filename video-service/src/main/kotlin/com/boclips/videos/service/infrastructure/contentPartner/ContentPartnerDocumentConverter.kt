package com.boclips.videos.service.infrastructure.contentPartner

import com.boclips.videos.service.domain.model.ageRange.AgeRange
import com.boclips.videos.service.domain.model.contentPartner.ContentPartner
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerId
import com.boclips.videos.service.domain.model.contentPartner.Credit
import org.bson.types.ObjectId

object ContentPartnerDocumentConverter {
    fun toContentPartnerDocument(contentPartner: ContentPartner): ContentPartnerDocument {
        val id: ObjectId
        val youtubeChanelId: String?

        if (isYoutubeChannelPartner(contentPartner.contentPartnerId)) {
            id = ObjectId.get()
            youtubeChanelId = contentPartner.contentPartnerId.value
        } else {
            id = ObjectId(contentPartner.contentPartnerId.value)
            youtubeChanelId = null
        }

        return ContentPartnerDocument(
            id = id,
            youtubeChannelId = youtubeChanelId,
            name = contentPartner.name,
            ageRangeMax = contentPartner.ageRange.max(),
            ageRangeMin = contentPartner.ageRange.min()
        )
    }

    fun toContentPartner(document: ContentPartnerDocument): ContentPartner {
        val id = document.youtubeChannelId ?: document.id.toString()

        return ContentPartner(
            contentPartnerId = ContentPartnerId(id),
            name = document.name,
            ageRange = if (document.ageRangeMin !== null) AgeRange.bounded(
                document.ageRangeMin,
                document.ageRangeMax
            ) else AgeRange.unbounded(),
            credit = document.youtubeChannelId?.let { Credit.YoutubeCredit(channelId = it) } ?: Credit.PartnerCredit
        )
    }

    fun isYoutubeChannelPartner(contentPartnerId: ContentPartnerId) =
        !ObjectId.isValid(contentPartnerId.value)
}