package com.boclips.contentpartner.service.application

import com.boclips.contentpartner.service.application.exceptions.ContentPartnerConflictException
import com.boclips.contentpartner.service.application.exceptions.InvalidContentCategoryException
import com.boclips.contentpartner.service.application.exceptions.InvalidAgeRangeException
import com.boclips.contentpartner.service.domain.model.DistributionMethod
import com.boclips.contentpartner.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.api.request.VideoServiceApiFactory
import com.boclips.videos.api.response.contentpartner.DistributionMethodResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Locale

class CreateContentPartnerIntegrationTest : AbstractSpringIntegrationTest() {
    @Test
    fun `content partners are searchable everywhere by default`() {
        val contentPartner = createContentPartner(
            VideoServiceApiFactory.createContentPartnerRequest(
                name = "My content partner",
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

    @Test
    fun `cannot create a content partner with an invalid content category`() {
        assertThrows<InvalidContentCategoryException> {
            createContentPartner(
                VideoServiceApiFactory.createContentPartnerRequest(
                    contentCategories = listOf("non existent")
                )
            )
        }
    }

    @Test
    fun `can create a content partner with a content category`() {
        val contentPartnerWithCategory = createContentPartner(
            VideoServiceApiFactory.createContentPartnerRequest(
                contentCategories = listOf("VIRTUAL_REALITY_360")
            )
        )

        assertThat(contentPartnerWithCategory.contentCategories).hasSize(1)
    }

    @Test
    fun `can create a content partner with a selected language`() {
        val contentPartnerWithCategory = createContentPartner(
            VideoServiceApiFactory.createContentPartnerRequest(
                language = "spa"
            )
        )

        val languageTag = Locale.forLanguageTag("spa");
        assertThat(contentPartnerWithCategory.language).isEqualTo(languageTag)
    }

    @Test
    fun `cannot create the same content partner twice`() {
        createContentPartner(VideoServiceApiFactory.createContentPartnerRequest())
        assertThrows<ContentPartnerConflictException> {
            createContentPartner(
                VideoServiceApiFactory.createContentPartnerRequest()
            )
        }
    }

    @Test
    fun `cannot create a content partner with an unrecognised age range bucket`() {
        assertThrows<InvalidAgeRangeException> {
            createContentPartner(
                VideoServiceApiFactory.createContentPartnerRequest(
                    ageRanges = listOf("A missing age range")
                )
            )
        }
    }

    @Test
    fun `can create a content partner with provided transcript `() {
        val isTranscriptProvided = true;

        val contentPartnerWithTranscript = createContentPartner(
            VideoServiceApiFactory.createContentPartnerRequest(
                isTranscriptProvided = isTranscriptProvided
            )
        )

        assertThat(contentPartnerWithTranscript.isTranscriptProvided).isEqualTo(isTranscriptProvided)
    }
}
