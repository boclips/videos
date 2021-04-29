package com.boclips.contentpartner.service.presentation.converters

import com.boclips.contentpartner.service.domain.model.channel.Taxonomy
import com.boclips.videos.service.domain.model.taxonomy.CategoryCode
import com.boclips.videos.service.domain.model.taxonomy.CategoryWithAncestors
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

internal class TaxonomyResourceConverterTest {
    @Test
    fun `converts a videoLevelTagging taxonomy`() {
        val taxonomy = Taxonomy.VideoLevelTagging

        val convertedTaxonomy = TaxonomyResourceConverter.toResource(taxonomy)!!

        assertThat(convertedTaxonomy.requiresVideoLevelTagging).isTrue
        assertThat(convertedTaxonomy.categories).isNull()
    }

    @Test
    fun `converts a channelLevelTagging taxonomy`() {
        val taxonomy = Taxonomy.ChannelLevelTagging(
            categories = setOf(
                CategoryWithAncestors(
                    ancestors = setOf(
                        CategoryCode("AB"), CategoryCode("A")
                    ), codeValue = CategoryCode("ABC"), description = "My category"
                )
            )
        )

        val convertedTaxonomy = TaxonomyResourceConverter.toResource(taxonomy)!!

        assertThat(convertedTaxonomy.requiresVideoLevelTagging).isNull()
        assertThat(convertedTaxonomy.categories!!.size).isEqualTo(1)
        assertThat(convertedTaxonomy.categories!!.first().codeValue).isEqualTo("ABC")
        assertThat(convertedTaxonomy.categories!!.first().description).isEqualTo("My category")
    }
}
