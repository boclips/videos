package com.boclips.videos.service.domain.service

import com.boclips.videos.service.domain.model.video.LegacyVideoType
import com.boclips.videos.service.domain.service.video.ContentEnrichers.Companion.isClassroom
import com.boclips.videos.service.testsupport.TestFactories.createVideo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ContentEnrichersTest {

    @Test
    fun `non stock content is matched as classroom for non-blacklisted content partners`() {
        assertThat(
            isClassroom(
                createVideo(
                    contentPartnerName = "Reuters",
                    type = LegacyVideoType.INSTRUCTIONAL_CLIPS
                )
            )
        ).isTrue()
    }

    @Test
    fun `stock content with the word "speech" is matched as classroom`() {
        assertThat(
            isClassroom(
                createVideo(
                    title = "jfk state of union speech",
                    type = LegacyVideoType.STOCK
                )
            )
        ).isTrue()
        assertThat(
            isClassroom(
                createVideo(
                    description = "jfk state of union Speech",
                    type = LegacyVideoType.STOCK
                )
            )
        ).isTrue()
    }

    @Test
    fun `stock content without the word "speech" is NOT matched as classroom`() {
        assertThat(
            isClassroom(
                createVideo(
                    title = "Family left speechless by bride's wedding dress",
                    type = LegacyVideoType.STOCK
                )
            )
        ).isFalse()
    }

    @Test
    fun `stock content with the phrase "archive public information film" is matched as classroom`() {
        assertThat(
            isClassroom(
                createVideo(
                    title = "Archive public information film: Woman throwing grain (to chickens)",
                    type = LegacyVideoType.STOCK
                )
            )
        ).isTrue()
    }

    @Test
    fun `stock content containing words "biology" and "animation" is matched as classroom`() {
        assertThat(
            isClassroom(
                createVideo(
                    title = "Animation explaining all about biology",
                    type = LegacyVideoType.STOCK
                )
            )
        ).isTrue()
    }

    @Test
    fun `stock content containing words "space" and "animation" is matched as classroom`() {
        assertThat(
            isClassroom(
                createVideo(
                    title = "Animation explaining all about space",
                    type = LegacyVideoType.STOCK
                )
            )
        ).isTrue()
    }

    @Test
    fun `stock featuring any of excluded words is NOT matched as classroom`() {
        val excluded = listOf(
            "award",
            "awards",
            "Cannes",
            "celebrities",
            "celebrity",
            "exchange",
            "hollywood",
            "naked",
            "nude",
            "party",
            "premier",
            "premiere",
            "sexy",
            "topless"
        )

        excluded.forEach {
            val title = "biology space animation $it"
            assertThat(
                isClassroom(
                    createVideo(
                        title = title,
                        type = LegacyVideoType.STOCK
                    )
                )
            ).withFailMessage("Expected '$title' to be excluded").isFalse()
        }
    }

    @Test
    fun `punctuation is stripped to allow correct matching of excluded words `() {
        val excludedTitles = listOf(
            "Premier: space animation",
            "Live at the space animation premier!",
            "space animation premier.",
            "space! animation! premier!"
        )

        excludedTitles.forEach {
            assertThat(
                isClassroom(
                    createVideo(
                        title = it,
                        type = LegacyVideoType.STOCK
                    )
                )
            ).withFailMessage("Expected '$it' to be excluded").isFalse()
        }
    }

    @Test
    fun `stock featuring the excluded phrases is NOT matched as classroom`() {
        val excluded = listOf(
            "red carpet",
            "Los Angeles"
        )

        excluded.forEach {
            val title = "biology space animation $it"
            assertThat(
                isClassroom(
                    createVideo(
                        title = title,
                        type = LegacyVideoType.STOCK
                    )
                )
            ).withFailMessage("Expected '$title' to be excluded").isFalse()
        }
    }
}
