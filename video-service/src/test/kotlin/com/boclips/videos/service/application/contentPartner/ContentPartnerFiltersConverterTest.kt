package com.boclips.videos.service.application.contentPartner

import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerFilter
import com.boclips.videos.service.domain.model.contentPartner.Credit
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ContentPartnerFiltersConverterTest {
    @Test
    fun `creates a name filter if present`() {
        val filters = ContentPartnerFiltersConverter.convert(name = "hello", accreditedYTChannelId = null)

        assertThat(filters).containsExactly(ContentPartnerFilter.NameFilter(name = "hello"))
    }

    @Test
    fun `creates an isOfficial filter if present`() {
        val filters = ContentPartnerFiltersConverter.convert(isOfficial = false, accreditedYTChannelId = null)

        assertThat(filters).containsExactly(ContentPartnerFilter.OfficialFilter(isOfficial = false))
    }

    @Test
    fun `creates an youtube channel id filter if present`() {
        val filters = ContentPartnerFiltersConverter.convert(accreditedYTChannelId = "123")

        assertThat(filters).containsExactly(ContentPartnerFilter.AccreditedTo(credit = Credit.YoutubeCredit(channelId = "123")))
    }
}
