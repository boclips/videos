package com.boclips.videos.service.application.contentPartner

import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerFilter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ContentPartnerFiltersConverterTest {
    @Test
    fun `creates a name filter if present`() {
        val filters = ContentPartnerFiltersConverter.convert(name = "hello")

        assertThat(filters).containsExactly(ContentPartnerFilter.NameFilter(name = "hello"))
    }

        @Test
    fun `creates an isOfficial filter if present`() {
        val filters = ContentPartnerFiltersConverter.convert(isOfficial = false)

        assertThat(filters).containsExactly(ContentPartnerFilter.OfficialFilter(isOfficial = false))
    }
}

