package com.boclips.videos.service.infrastructure.event

import com.boclips.videos.service.infrastructure.event.types.User
import com.boclips.videos.service.testsupport.setSecurityContext
import com.sun.security.auth.UserPrincipal
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.security.core.userdetails.UserDetails

internal class UserExtractorTest{

    @Test
    fun `when principal extracts mail from name`() {
        val boclipsUsername = "test@boclips.com"
        setSecurityContext(UserPrincipal(boclipsUsername))
        assertThat(UserExtractor.getCurrentUser()).isEqualTo(User.fromEmail(boclipsUsername))
    }

    @Test
    fun `when userDetails extracts mail from username`() {
        val boclipsUsername = "test@boclips.com"
        setSecurityContext(org.springframework.security.core.userdetails.User(boclipsUsername,"password", emptyList()))
        assertThat(UserExtractor.getCurrentUser()).isEqualTo(User.fromEmail(boclipsUsername))
    }

    @Test
    fun `when string extracts mail from value`() {
        val boclipsUsername = "test@boclips.com"
        setSecurityContext(boclipsUsername)
        assertThat(UserExtractor.getCurrentUser()).isEqualTo(User.fromEmail(boclipsUsername))
    }
}