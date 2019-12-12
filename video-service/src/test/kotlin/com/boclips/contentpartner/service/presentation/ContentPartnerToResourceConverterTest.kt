package com.boclips.contentpartner.service.presentation

import com.boclips.contentpartner.service.domain.model.Credit
import com.boclips.contentpartner.service.domain.model.Remittance
import com.boclips.contentpartner.service.testsupport.TestFactories
import com.boclips.videos.service.domain.model.video.DistributionMethod
import com.boclips.videos.service.presentation.deliveryMethod.DistributionMethodResource
import com.boclips.videos.service.testsupport.UserFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.Currency

class ContentPartnerToResourceConverterTest {
    @Test
    fun `convert content partner to resource`() {
        val user = UserFactory.sample(isAdministrator = true)

        val contentPartner = TestFactories.createContentPartner(
            credit = Credit.PartnerCredit,
            legalRestrictions = TestFactories.createLegalRestrictions(text = "Forbidden in the EU"),
            distributionMethods = setOf(DistributionMethod.STREAM),
            remittance = Remittance(Currency.getInstance("GBP"))
        )

        val contentPartnerResource = ContentPartnerToResourceConverter.convert(contentPartner, user)

        assertThat(contentPartnerResource.id).isNotEmpty()
        assertThat(contentPartnerResource.name).isNotEmpty()
        assertThat(contentPartnerResource.ageRange).isNotNull
        assertThat(contentPartnerResource.official).isTrue()
        assertThat(contentPartnerResource.legalRestrictions).isNotNull
        assertThat(contentPartnerResource.legalRestrictions?.text).isEqualTo("Forbidden in the EU")
        assertThat(contentPartnerResource.distributionMethods).isEqualTo(setOf(DistributionMethodResource.STREAM))
        assertThat(contentPartnerResource.currency).isEqualTo("GBP")
    }

    @Test
    fun `converted content partner resource has no currency if user does not have backoffice role`() {
        val user = UserFactory.sample()

        val contentPartner = TestFactories.createContentPartner(
            credit = Credit.PartnerCredit,
            legalRestrictions = TestFactories.createLegalRestrictions(text = "Forbidden in the EU"),
            distributionMethods = setOf(DistributionMethod.STREAM),
            remittance = Remittance(Currency.getInstance("GBP"))
        )

        val contentPartnerResource = ContentPartnerToResourceConverter.convert(contentPartner, user)

        assertThat(contentPartnerResource.id).isNotEmpty()
        assertThat(contentPartnerResource.currency).isEqualTo(null)
    }
}
