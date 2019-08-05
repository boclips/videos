package com.boclips.videos.service.application.contentPartner

import com.boclips.videos.service.domain.model.video.DistributionMethod
import com.boclips.videos.service.presentation.ageRange.AgeRangeRequest
import com.boclips.videos.service.presentation.deliveryMethod.DistributionMethodResource
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CreateContentPartnerIntegrationTest : AbstractSpringIntegrationTest() {
    @Test
    fun `content partners are searchable everywhere by default`() {
        val contentPartner = createContentPartner(
            TestFactories.createContentPartnerRequest(
                name = "My content partner",
                ageRange = AgeRangeRequest(min = 7, max = 11),
                distributionMethods = null
            )
        )

        assertThat(contentPartner.distributionMethods).isEqualTo(DistributionMethod.ALL)
    }

    @Test
    fun `mark content partners available for stream and download`() {
        val contentPartner = createContentPartner(
            TestFactories.createContentPartnerRequest(
                name = "My content partner",
                ageRange = AgeRangeRequest(min = 7, max = 11),
                distributionMethods = setOf(
                    DistributionMethodResource.DOWNLOAD,
                    DistributionMethodResource.STREAM
                )
            )
        )

        assertThat(contentPartner.distributionMethods).isEqualTo(DistributionMethod.ALL)
    }

    @Test
    fun `videos are searchable when distribution methods are not specified`() {
        val contentPartner = createContentPartner(
            TestFactories.createContentPartnerRequest(
                name = "My content partner",
                ageRange = AgeRangeRequest(min = 7, max = 11),
                distributionMethods = null
            )
        )

        assertThat(contentPartner.distributionMethods).isEqualTo(DistributionMethod.ALL)
    }

    @Test
    fun `can create an official content partner with the same name as a youtube content partner`() {
        val youtubeContentPartner = createContentPartner(
            TestFactories.createContentPartnerRequest(
                name = "Tsitsipas",
                accreditedToYtChannel = "23456789"
            )
        )

        val officialContentPartner = createContentPartner(
            TestFactories.createContentPartnerRequest(
                name = "Tsitsipas",
                accreditedToYtChannel = null
            )
        )

        assertThat(officialContentPartner.contentPartnerId).isNotEqualTo(youtubeContentPartner)
    }
}
