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
            searchable = false,
            hiddenFromSearchForDeliveryMethods = setOf(DeliveryMethod.DOWNLOAD)
        )

        val document = ContentPartnerDocumentConverter.toContentPartnerDocument(original)
        val convertedAsset = ContentPartnerDocumentConverter.toContentPartner(document)

        assertThat(convertedAsset).isEqualTo(original)
    }

    @Test
    fun `the content partner is searchable by default`() {
        val document = ContentPartnerDocument(
            id = ObjectId.get(),
            name = "content partner",
            searchable = null,
            ageRangeMax = null,
            ageRangeMin = null
        )

        val convertedAsset = ContentPartnerDocumentConverter.toContentPartner(document)
        assertThat(convertedAsset.searchable).isTrue()
    }

    @Test
    fun `content partner defaults to searchable when delivery methods is not present`() {
        val document = ContentPartnerDocument(
            id = ObjectId.get(),
            name = "content partner",
            searchable = false,
            ageRangeMax = null,
            ageRangeMin = null,
            hiddenFromSearchForDeliveryMethods = null
        )

        val convertedAsset = ContentPartnerDocumentConverter.toContentPartner(document)
        assertThat(convertedAsset.searchable).isFalse()
        assertThat(convertedAsset.hiddenFromSearchForDeliveryMethods).isEqualTo(DeliveryMethod.ALL)
    }
}