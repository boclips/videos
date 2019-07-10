package com.boclips.videos.service.domain.model.contentPartner

import com.boclips.videos.service.domain.model.video.DeliveryMethod
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ContentPartnerTest {

    @Test
    fun `is streamable if not hidden from stream`() {
        val contentPartner = TestFactories.createContentPartner(hiddenFromSearchForDeliveryMethods = emptySet())

        assertThat(contentPartner.isStreamable()).isTrue()
    }

    @Test
    fun `is downloadable if not hidden for download`() {
        val contentPartner = TestFactories.createContentPartner(hiddenFromSearchForDeliveryMethods = emptySet())

        assertThat(contentPartner.isDownloadable()).isTrue()
    }

    @Test
    fun `is not downloadable nor streamable`() {
        val contentPartner =
            TestFactories.createContentPartner(hiddenFromSearchForDeliveryMethods = DeliveryMethod.ALL)

        assertThat(contentPartner.isStreamable()).isFalse()
        assertThat(contentPartner.isDownloadable()).isFalse()
    }
}
