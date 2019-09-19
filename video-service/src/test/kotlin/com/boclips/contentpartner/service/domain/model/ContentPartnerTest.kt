package com.boclips.contentpartner.service.domain.model

import com.boclips.contentpartner.service.testsupport.TestFactories
import com.boclips.videos.service.domain.model.video.DistributionMethod
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ContentPartnerTest {

    @Test
    fun `is streamable if not hidden from stream`() {
        val contentPartner = TestFactories.createContentPartner(distributionMethods = setOf(DistributionMethod.STREAM))

        assertThat(contentPartner.isStreamable()).isTrue()
    }

    @Test
    fun `is downloadable if not hidden for download`() {
        val contentPartner =
            TestFactories.createContentPartner(distributionMethods = setOf(DistributionMethod.DOWNLOAD))

        assertThat(contentPartner.isDownloadable()).isTrue()
    }

    @Test
    fun `is not downloadable nor streamable`() {
        val contentPartner =
            TestFactories.createContentPartner(distributionMethods = emptySet())

        assertThat(contentPartner.isStreamable()).isFalse()
        assertThat(contentPartner.isDownloadable()).isFalse()
    }
}
