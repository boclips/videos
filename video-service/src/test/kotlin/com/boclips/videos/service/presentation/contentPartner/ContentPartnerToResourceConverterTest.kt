package com.boclips.videos.service.presentation.contentPartner

import com.boclips.videos.service.domain.model.contentPartner.Credit
import com.boclips.videos.service.domain.model.video.DistributionMethod
import com.boclips.videos.service.presentation.deliveryMethod.DeliveryMethodResource
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ContentPartnerToResourceConverterTest {
    @Test
    fun `convert content partner to resource`() {
        val contentPartner = TestFactories.createContentPartner(
            credit = Credit.PartnerCredit,
            distributionMethods = setOf(DistributionMethod.STREAM)
        )

        val contentPartnerResource = ContentPartnerToResourceConverter.convert(contentPartner)

        assertThat(contentPartnerResource.id).isNotEmpty()
        assertThat(contentPartnerResource.name).isNotEmpty()
        assertThat(contentPartnerResource.ageRange).isNotNull
        assertThat(contentPartnerResource.isOfficial).isTrue()
        assertThat(contentPartnerResource.hiddenFromSearchForDeliveryMethods).isEqualTo(setOf(DeliveryMethodResource.DOWNLOAD))
    }
}
