package com.boclips.videos.service.infrastructure.contentPartner

import com.boclips.videos.service.domain.model.common.AgeRange
import com.boclips.videos.service.domain.model.contentPartner.ContentPartner
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerId
import com.boclips.videos.service.domain.model.contentPartner.Credit
import com.boclips.videos.service.domain.model.video.DeliveryMethod
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test

internal class ContentPartnerDocumentConverterTest {

    @Test
    fun `round-trips a content partner conversion via document`() {
        val original = ContentPartner(
            contentPartnerId = ContentPartnerId(value = ObjectId().toHexString()),
            name = "The grandest content partner there ever lived",
            ageRange = AgeRange.bounded(5, 11),
            credit = Credit.PartnerCredit,
            hiddenFromSearchForDeliveryMethods = setOf(DeliveryMethod.DOWNLOAD)
        )

        val document = ContentPartnerDocumentConverter.toContentPartnerDocument(original)
        val convertedAsset = ContentPartnerDocumentConverter.toContentPartner(document)

        assertThat(convertedAsset).isEqualTo(original)
    }

    @Test
    fun `the content partner is not hidden by default`() {
        val document = ContentPartnerDocument(
            id = ObjectId.get(),
            name = "content partner",
            ageRangeMax = null,
            ageRangeMin = null
        )

        val convertedAsset = ContentPartnerDocumentConverter.toContentPartner(document)
        assertThat(convertedAsset.hiddenFromSearchForDeliveryMethods).isEmpty()
    }
}
