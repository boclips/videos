package com.boclips.videos.service.application.contentPartner

import com.boclips.contentpartner.service.application.ContentPartnerFiltersConverter
import com.boclips.contentpartner.service.domain.model.ContentPartnerFilter
import com.boclips.contentpartner.service.domain.model.Credit
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ContentPartnerFiltersConverterTest {
    @Test
    fun `creates a name filter if present`() {
        val filters = ContentPartnerFiltersConverter.convert(name = "hello", accreditedYTChannelId = null)

        assertThat(filters).containsExactly(ContentPartnerFilter.NameFilter(name = "hello"))
    }

    @Test
    fun `creates an official filter if present`() {
        val filters = ContentPartnerFiltersConverter.convert(official = false, accreditedYTChannelId = null)

        assertThat(filters).containsExactly(ContentPartnerFilter.OfficialFilter(official = false))
    }

    @Test
    fun `creates an youtube channel id filter if present`() {
        val filters = ContentPartnerFiltersConverter.convert(accreditedYTChannelId = "123")

        assertThat(filters).containsExactly(ContentPartnerFilter.AccreditedTo(credit = Credit.YoutubeCredit(channelId = "123")))
    }
}

