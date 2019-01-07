package com.boclips.videos.service.domain.service.ContentFilterssisInTeacherProduct

import com.boclips.videos.service.domain.model.ContentEnrichers
import com.boclips.videos.service.domain.model.asset.VideoType
import com.boclips.videos.service.testsupport.TestFactories.createVideoAsset
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


class ContentEnrichersTest {

    @Test
    fun `non stock content is marked as educational`() {
        val video = createVideoAsset(type = VideoType.INSTRUCTIONAL_CLIPS)

        assertThat(ContentEnrichers.isNonEducationalStock(video)).isFalse()
    }

    @Test
    fun `stock content with the word "speech" is marked as educational`() {
        assertThat(ContentEnrichers.isNonEducationalStock(createVideoAsset(type = VideoType.STOCK, title = "jfk state of union speech"))).isFalse()
        assertThat(ContentEnrichers.isNonEducationalStock(createVideoAsset(type = VideoType.STOCK, description = "jfk state of union Speech"))).isFalse()
    }

    @Test
    fun `stock content without the word "speech" is not marked as educational`() {
        assertThat(ContentEnrichers.isNonEducationalStock(createVideoAsset(type = VideoType.STOCK, title = "Family left speechless by bride's wedding dress"))).isTrue()
    }

    @Test
    fun `stock content with the phrase "archive public information film" is marked as educational`() {
        assertThat(ContentEnrichers.isNonEducationalStock(createVideoAsset(type = VideoType.STOCK, title = "Archive public information film: Woman throwing grain (to chickens)"))).isFalse()
    }

    @Test
    fun `stock content containing words "biology" and "animation" is marked as educational`() {
        assertThat(ContentEnrichers.isNonEducationalStock(createVideoAsset(type = VideoType.STOCK, title = "Animation explaining all about biology"))).isFalse()
    }

    @Test
    fun `stock content containing words "space" and "animation" is marked as educational`() {
        assertThat(ContentEnrichers.isNonEducationalStock(createVideoAsset(type = VideoType.STOCK, title = "Animation explaining all about space"))).isFalse()
    }

    @Test
    fun `stock content containing the word "awards" is marked not educational`() {
        assertThat(ContentEnrichers.isNonEducationalStock(createVideoAsset(type = VideoType.STOCK, title = "speech archive public information film biology space animation awards"))).isTrue()
    }

    @Test
    fun `stock content containing the word "premiere" is marked not educational`() {
        assertThat(ContentEnrichers.isNonEducationalStock(createVideoAsset(type = VideoType.STOCK, title = "speech archive public information film biology space animation premiere"))).isTrue()
    }

    @Test
    fun `stock content containing the phrase "red carpet" is marked not educational`() {
        assertThat(ContentEnrichers.isNonEducationalStock(createVideoAsset(type = VideoType.STOCK, title = "speech archive public information film biology space animation red carpet"))).isTrue()
        assertThat(ContentEnrichers.isNonEducationalStock(createVideoAsset(type = VideoType.STOCK, title = "speech archive public information film biology space animation red colour carpet"))).isFalse()
    }
}