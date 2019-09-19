package com.boclips.contentpartner.service.presentation

import com.boclips.contentpartner.service.domain.model.Credit
import com.boclips.contentpartner.service.testsupport.TestFactories
import com.boclips.videos.service.domain.model.video.DistributionMethod
import com.boclips.videos.service.presentation.deliveryMethod.DistributionMethodResource
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
        assertThat(contentPartnerResource.official).isTrue()
        assertThat(contentPartnerResource.distributionMethods).isEqualTo(setOf(DistributionMethodResource.STREAM))
    }
}