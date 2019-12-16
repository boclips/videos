package com.boclips.videos.service.domain.service

import com.boclips.videos.service.domain.model.video.ContentType
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
                    type = ContentType.INSTRUCTIONAL_CLIPS
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
                    type = ContentType.STOCK
                )
            )
        ).isTrue()
        assertThat(
            isClassroom(
                createVideo(
                    description = "jfk state of union Speech",
                    type = ContentType.STOCK
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
                    type = ContentType.STOCK
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
                    type = ContentType.STOCK
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
                    type = ContentType.STOCK
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
                    type = ContentType.STOCK
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
                        type = ContentType.STOCK
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
                        type = ContentType.STOCK
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
            val video = createVideo(title = title, type = ContentType.STOCK)

            assertThat(isClassroom(video)).withFailMessage("Expected '$title' to be excluded").isFalse()
        }
    }

    @Test
    fun `excludes videos with specific ids`() {
        val video = createVideo(videoId = "5c54d6a2d8eafeecae205289", type = ContentType.INSTRUCTIONAL_CLIPS)

        assertThat(isClassroom(video)).withFailMessage("Expected '${video.videoId}' to be excluded").isFalse()
    }
}
