package com.boclips.videos.service.domain.model

import com.boclips.security.testing.setSecurityContext
import com.boclips.videos.service.domain.model.common.UserId
import com.boclips.videos.service.domain.model.video.UserRating
import com.boclips.videos.service.testsupport.TestFactories.createVideo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class VideoTest {

    @Test
    fun `average returns rating average`() {
        val video = createVideo(ratings = listOf(UserRating(1, UserId("1")), UserRating(2, UserId("2"))))

        assertThat(video.getRatingAverage()).isEqualTo(1.5)
    }

    @Test
    fun `average returns null when empty list`() {
        val video = createVideo(ratings = emptyList())

        assertThat(video.getRatingAverage()).isNull()
    }

    @Test
    fun `is rated by user when no user`() {
        val video = createVideo(ratings = listOf(UserRating(rating = 3, userId = UserId("another-teacher"))))

        assertThat(video.isRatedByCurrentUser()).isFalse()
    }

    @Test
    fun `is rated by user when current user`() {
        setSecurityContext("teacher")

        val video = createVideo(ratings = listOf(UserRating(rating = 3, userId = UserId("teacher"))))

        assertThat(video.isRatedByCurrentUser()).isTrue()
    }

    @Test
    fun `is rated by user when other user`() {
        setSecurityContext("teacher")

        val video = createVideo(ratings = listOf(UserRating(rating = 3, userId = UserId("anothertheacher"))))

        assertThat(video.isRatedByCurrentUser()).isFalse()
    }
}