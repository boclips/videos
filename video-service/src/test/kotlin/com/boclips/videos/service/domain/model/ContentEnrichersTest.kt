package com.boclips.videos.service.domain.model

import com.boclips.videos.service.domain.model.ContentEnrichers.Companion.isClassroom
import com.boclips.videos.service.domain.model.video.LegacyVideoType
import com.boclips.videos.service.testsupport.TestFactories.createVideo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ContentEnrichersTest {

    @Test
    fun `blacklisted content partners of any type are not classroom`() {
        val blackListedContentPartners = listOf(
            "AP",
            "numberock",
            "Siren Films",
            "StoryFul",
            "Singapore Press Holdings",
            "Mage Math",
            "engVid",
            "1 Minute in a Museum",
            "British Movietone",
            "TED Talks",
            "TED-Ed"
        )

        blackListedContentPartners.forEach {
            assertThat(
                isClassroom(
                    createVideo(
                        contentPartnerId = it
                    )
                )
            ).withFailMessage("Expected $it to be excluded").isFalse()
        }
    }

    @Test
    fun `non stock content is matched as classroom for non-blacklisted content partners`() {
        assertThat(
            isClassroom(
                createVideo(
                    type = LegacyVideoType.INSTRUCTIONAL_CLIPS,
                    contentPartnerId = "Reuters"
                )
            )
        ).isTrue()
    }

    @Test
    fun `stock content with the word "speech" is matched as classroom`() {
        assertThat(
            isClassroom(
                createVideo(
                    type = LegacyVideoType.STOCK,
                    title = "jfk state of union speech"
                )
            )
        ).isTrue()
        assertThat(
            isClassroom(
                createVideo(
                    type = LegacyVideoType.STOCK,
                    description = "jfk state of union Speech"
                )
            )
        ).isTrue()
    }

    @Test
    fun `stock content without the word "speech" is NOT matched as classroom`() {
        assertThat(
            isClassroom(
                createVideo(
                    type = LegacyVideoType.STOCK,
                    title = "Family left speechless by bride's wedding dress"
                )
            )
        ).isFalse()
    }

    @Test
    fun `stock content with the phrase "archive public information film" is matched as classroom`() {
        assertThat(
            isClassroom(
                createVideo(
                    type = LegacyVideoType.STOCK,
                    title = "Archive public information film: Woman throwing grain (to chickens)"
                )
            )
        ).isTrue()
    }

    @Test
    fun `stock content containing words "biology" and "animation" is matched as classroom`() {
        assertThat(
            isClassroom(
                createVideo(
                    type = LegacyVideoType.STOCK,
                    title = "Animation explaining all about biology"
                )
            )
        ).isTrue()
    }

    @Test
    fun `stock content containing words "space" and "animation" is matched as classroom`() {
        assertThat(
            isClassroom(
                createVideo(
                    type = LegacyVideoType.STOCK,
                    title = "Animation explaining all about space"
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
                        type = LegacyVideoType.STOCK,
                        title = title
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
                        type = LegacyVideoType.STOCK,
                        title = it
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
                        type = LegacyVideoType.STOCK,
                        title = title
                    )
                )
            ).withFailMessage("Expected '$title' to be excluded").isFalse()
        }
    }
}
