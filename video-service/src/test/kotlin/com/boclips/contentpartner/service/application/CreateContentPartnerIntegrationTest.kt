package com.boclips.contentpartner.service.application

import com.boclips.contentpartner.service.domain.model.DistributionMethod
import com.boclips.contentpartner.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.api.request.VideoServiceApiFactory
import com.boclips.videos.api.request.contentpartner.AgeRangeRequest
import com.boclips.videos.api.response.contentpartner.DistributionMethodResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CreateContentPartnerIntegrationTest : AbstractSpringIntegrationTest() {
    @Test
    fun `content partners are searchable everywhere by default`() {
        val contentPartner = createContentPartner(
            VideoServiceApiFactory.createContentPartnerRequest(
                name = "My content partner",
                ageRange = AgeRangeRequest(
                    min = 7,
                    max = 11
                ),
                distributionMethods = null
            )
        )

        assertThat(contentPartner.distributionMethods).isEqualTo(DistributionMethod.ALL)
    }

    @Test
    fun `mark content partners available for stream and download`() {
        val contentPartner = createContentPartner(
            VideoServiceApiFactory.createContentPartnerRequest(
                name = "My content partner",
                ageRange = AgeRangeRequest(
                    min = 7,
                    max = 11
                ),
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
            VideoServiceApiFactory.createContentPartnerRequest(
                name = "My content partner",
                ageRange = AgeRangeRequest(
                    min = 7,
                    max = 11
                ),
                distributionMethods = null
            )
        )

        assertThat(contentPartner.distributionMethods).isEqualTo(DistributionMethod.ALL)
    }

    @Test
    fun `can create an official content partner with the same name as a youtube content partner`() {
        val youtubeContentPartner = createContentPartner(
            VideoServiceApiFactory.createContentPartnerRequest(
                name = "Tsitsipas",
                accreditedToYtChannel = "23456789"
            )
        )

        val officialContentPartner = createContentPartner(
            VideoServiceApiFactory.createContentPartnerRequest(
                name = "Tsitsipas",
                accreditedToYtChannel = null
            )
        )

        assertThat(officialContentPartner.contentPartnerId).isNotEqualTo(youtubeContentPartner)
    }
}
