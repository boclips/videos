package com.boclips.contentpartner.service.domain.model

import com.boclips.contentpartner.service.domain.model.contentpartner.DistributionMethod
import com.boclips.contentpartner.service.testsupport.ContentPartnerFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ContentPartnerTest {

    @Test
    fun `is streamable if not hidden from stream`() {
        val contentPartner = ContentPartnerFactory.createContentPartner(distributionMethods = setOf(DistributionMethod.STREAM))

        assertThat(contentPartner.isStreamable()).isTrue()
    }

    @Test
    fun `is downloadable if not hidden for download`() {
        val contentPartner =
            ContentPartnerFactory.createContentPartner(distributionMethods = setOf(DistributionMethod.DOWNLOAD))

        assertThat(contentPartner.isDownloadable()).isTrue()
    }

    @Test
    fun `is not downloadable nor streamable`() {
        val contentPartner =
            ContentPartnerFactory.createContentPartner(distributionMethods = emptySet())

        assertThat(contentPartner.isStreamable()).isFalse()
        assertThat(contentPartner.isDownloadable()).isFalse()
    }
}
