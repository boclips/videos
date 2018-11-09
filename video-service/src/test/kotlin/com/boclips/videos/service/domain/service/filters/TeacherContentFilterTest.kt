package com.boclips.videos.service.domain.service.filters

import com.boclips.videos.service.domain.model.VideoType
import com.boclips.videos.service.testsupport.TestFactories.createVideoDetails
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


class TeacherContentFilterTest {

    val filter = TeacherContentFilter()

    @Test
    fun `non stock content is included`() {
        val video = createVideoDetails(type = VideoType.INSTRUCTIONAL_CLIPS)

        assertThat(filter.showInTeacherProduct(video)).isTrue()
    }

    @Test
    fun `stock content with the word "speech" is included`() {
        assertThat(filter.showInTeacherProduct(createVideoDetails(type = VideoType.STOCK, title = "jfk state of union speech"))).isTrue()
        assertThat(filter.showInTeacherProduct(createVideoDetails(type = VideoType.STOCK, description = "jfk state of union Speech"))).isTrue()
    }

    @Test
    fun `stock content without the word "speech" is not included`() {
        assertThat(filter.showInTeacherProduct(createVideoDetails(type = VideoType.STOCK, title = "Family left speechless by bride's wedding dress"))).isFalse()
    }

    @Test
    fun `stock content with the phrase "archive public information film" is included`() {
        assertThat(filter.showInTeacherProduct(createVideoDetails(type = VideoType.STOCK, title = "Archive public information film: Woman throwing grain (to chickens)"))).isTrue()
    }

    @Test
    fun `stock content containing words "biology" and "animation" is included`() {
        assertThat(filter.showInTeacherProduct(createVideoDetails(type = VideoType.STOCK, title = "Animation explaining all about biology"))).isTrue()
    }

    @Test
    fun `stock content containing words "space" and "animation" is included`() {
        assertThat(filter.showInTeacherProduct(createVideoDetails(type = VideoType.STOCK, title = "Animation explaining all about space"))).isTrue()
    }

    @Test
    fun `stock content containing the word "awards" is never included`() {
        assertThat(filter.showInTeacherProduct(createVideoDetails(type = VideoType.STOCK, title = "speech archive public information film biology space animation awards"))).isFalse()
    }

    @Test
    fun `stock content containing the word "premiere" is never included`() {
        assertThat(filter.showInTeacherProduct(createVideoDetails(type = VideoType.STOCK, title = "speech archive public information film biology space animation premiere"))).isFalse()
    }

    @Test
    fun `stock content containing the phrase "red carpet" is never included`() {
        assertThat(filter.showInTeacherProduct(createVideoDetails(type = VideoType.STOCK, title = "speech archive public information film biology space animation red carpet"))).isFalse()
        assertThat(filter.showInTeacherProduct(createVideoDetails(type = VideoType.STOCK, title = "speech archive public information film biology space animation red colour carpet"))).isTrue()
    }


}