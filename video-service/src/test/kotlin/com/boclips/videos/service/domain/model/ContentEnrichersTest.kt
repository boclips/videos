package com.boclips.videos.service.domain.model

import com.boclips.videos.service.domain.model.asset.LegacyVideoType
import com.boclips.videos.service.testsupport.TestFactories.createVideoAsset
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


class ContentEnrichersTest {

    @Test
    fun `non stock content is matched as classroom`() {
        val video = createVideoAsset(type = LegacyVideoType.INSTRUCTIONAL_CLIPS)

        assertThat(ContentEnrichers.isClassroom(video)).isTrue()
    }

    @Test
    fun `stock content with the word "speech" is matched as classroom`() {
        assertThat(ContentEnrichers.isClassroom(createVideoAsset(type = LegacyVideoType.STOCK, title = "jfk state of union speech"))).isTrue()
        assertThat(ContentEnrichers.isClassroom(createVideoAsset(type = LegacyVideoType.STOCK, description = "jfk state of union Speech"))).isTrue()
    }

    @Test
    fun `stock content without the word "speech" is NOT matched as classroom`() {
        assertThat(ContentEnrichers.isClassroom(createVideoAsset(type = LegacyVideoType.STOCK, title = "Family left speechless by bride's wedding dress"))).isFalse()
    }

    @Test
    fun `stock content with the phrase "archive public information film" is matched as classroom`() {
        assertThat(ContentEnrichers.isClassroom(createVideoAsset(type = LegacyVideoType.STOCK, title = "Archive public information film: Woman throwing grain (to chickens)"))).isTrue()
    }

    @Test
    fun `stock content containing words "biology" and "animation" is matched as classroom`() {
        assertThat(ContentEnrichers.isClassroom(createVideoAsset(type = LegacyVideoType.STOCK, title = "Animation explaining all about biology"))).isTrue()
    }

    @Test
    fun `stock content containing words "space" and "animation" is matched as classroom`() {
        assertThat(ContentEnrichers.isClassroom(createVideoAsset(type = LegacyVideoType.STOCK, title = "Animation explaining all about space"))).isTrue()
    }

    @Test
    fun `stock content containing the word "awards" is NOT matched as classroom`() {
        assertThat(ContentEnrichers.isClassroom(createVideoAsset(type = LegacyVideoType.STOCK, title = "speech archive public information film biology space animation awards"))).isFalse()
    }

    @Test
    fun `stock content containing the word "premiere" is NOT matched as classroom`() {
        assertThat(ContentEnrichers.isClassroom(createVideoAsset(type = LegacyVideoType.STOCK, title = "speech archive public information film biology space animation premiere"))).isFalse()
    }

    @Test
    fun `stock content containing the phrase "red carpet" is NOT matched as classroom`() {
        assertThat(ContentEnrichers.isClassroom(createVideoAsset(type = LegacyVideoType.STOCK, title = "speech archive public information film biology space animation red carpet"))).isFalse()
        assertThat(ContentEnrichers.isClassroom(createVideoAsset(type = LegacyVideoType.STOCK, title = "speech archive public information film biology space animation red colour carpet"))).isTrue()
    }
}