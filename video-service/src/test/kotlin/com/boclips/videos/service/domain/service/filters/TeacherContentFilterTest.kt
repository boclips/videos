package com.boclips.videos.service.domain.service.ContentFilterssisInTeacherProduct

import com.boclips.videos.service.domain.model.asset.VideoType
import com.boclips.videos.service.domain.service.ContentFilters
import com.boclips.videos.service.testsupport.TestFactories.createVideoAsset
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


class TeacherContentFilterTest {

    @Test
    fun `non stock content is included`() {
        val video = createVideoAsset(type = VideoType.INSTRUCTIONAL_CLIPS)

        assertThat(ContentFilters.isInTeacherProduct(video)).isTrue()
    }

    @Test
    fun `stock content with the word "speech" is included`() {
        assertThat(ContentFilters.isInTeacherProduct(createVideoAsset(type = VideoType.STOCK, title = "jfk state of union speech"))).isTrue()
        assertThat(ContentFilters.isInTeacherProduct(createVideoAsset(type = VideoType.STOCK, description = "jfk state of union Speech"))).isTrue()
    }

    @Test
    fun `stock content without the word "speech" is not included`() {
        assertThat(ContentFilters.isInTeacherProduct(createVideoAsset(type = VideoType.STOCK, title = "Family left speechless by bride's wedding dress"))).isFalse()
    }

    @Test
    fun `stock content with the phrase "archive public information film" is included`() {
        assertThat(ContentFilters.isInTeacherProduct(createVideoAsset(type = VideoType.STOCK, title = "Archive public information film: Woman throwing grain (to chickens)"))).isTrue()
    }

    @Test
    fun `stock content containing words "biology" and "animation" is included`() {
        assertThat(ContentFilters.isInTeacherProduct(createVideoAsset(type = VideoType.STOCK, title = "Animation explaining all about biology"))).isTrue()
    }

    @Test
    fun `stock content containing words "space" and "animation" is included`() {
        assertThat(ContentFilters.isInTeacherProduct(createVideoAsset(type = VideoType.STOCK, title = "Animation explaining all about space"))).isTrue()
    }

    @Test
    fun `stock content containing the word "awards" is never included`() {
        assertThat(ContentFilters.isInTeacherProduct(createVideoAsset(type = VideoType.STOCK, title = "speech archive public information film biology space animation awards"))).isFalse()
    }

    @Test
    fun `stock content containing the word "premiere" is never included`() {
        assertThat(ContentFilters.isInTeacherProduct(createVideoAsset(type = VideoType.STOCK, title = "speech archive public information film biology space animation premiere"))).isFalse()
    }

    @Test
    fun `stock content containing the phrase "red carpet" is never included`() {
        assertThat(ContentFilters.isInTeacherProduct(createVideoAsset(type = VideoType.STOCK, title = "speech archive public information film biology space animation red carpet"))).isFalse()
        assertThat(ContentFilters.isInTeacherProduct(createVideoAsset(type = VideoType.STOCK, title = "speech archive public information film biology space animation red colour carpet"))).isTrue()
    }


}