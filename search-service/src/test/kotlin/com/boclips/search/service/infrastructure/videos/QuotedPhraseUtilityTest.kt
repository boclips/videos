package com.boclips.search.service.infrastructure.videos

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class QuotedPhraseUtilityTest {
    @Test
    fun `changes nothing if there are no quotes`() {
        val parts = "here        is   a  phrase".quotedParts()
        assertThat(parts.quotedParts).isEmpty()
        assertEquals("here        is   a  phrase", parts.unquoted)
    }

    @Test
    fun `splits single quoted phrase at beginning of input`() {
        val parts = "\"quoted\" unquoted".quotedParts()
        assertThat(parts.quotedParts).containsExactly("quoted")
        assertEquals("quoted unquoted", parts.unquoted)
    }

    @Test
    fun `splits single quoted phrase at end of input`() {
        val parts = "unquoted \"quoted\"".quotedParts()
        assertThat(parts.quotedParts).containsExactly("quoted")
        assertEquals("unquoted quoted", parts.unquoted)
    }

    @Test
    fun `splits single quoted phrase in middle of input`() {
        val parts = "unquoted1 \"quoted\" unquoted2".quotedParts()
        assertThat(parts.quotedParts).containsExactly("quoted")
        assertEquals("unquoted1 quoted unquoted2", parts.unquoted)
    }

    @Test
    fun `splits both quoted phrases in middle of input`() {
        val parts = "unquoted1 \"quoted1\" unquoted2 \"quoted2\" unquoted3".quotedParts()
        assertThat(parts.quotedParts).containsExactly("quoted1", "quoted2")
        assertEquals("unquoted1 quoted1 unquoted2 quoted2 unquoted3", parts.unquoted)
    }

    @Test
    fun `strips single quotes`() {
        val parts = "cat \"  dog".quotedParts()
        assertThat(parts.quotedParts).isEmpty()
        assertEquals("cat   dog", parts.unquoted)
    }

    @Test
    fun `strips last quote in odd numbers of quotes`() {
        val parts = "cat \"dog\" mouse\" chicken".quotedParts()
        assertThat(parts.quotedParts).containsExactly("dog")
        assertEquals("cat dog mouse chicken", parts.unquoted)
    }

    @Test
    fun `allows zero-length quotations`() {
        val parts = "some phrase \"\" goes here".quotedParts()
        assertThat(parts.quotedParts).containsExactly("")
        assertEquals("some phrase  goes here", parts.unquoted)
    }

    @Test
    fun `allows zero-length quotations by themselves`() {
        val parts = "\"\"".quotedParts()
        assertThat(parts.quotedParts).containsExactly("")
        assertEquals("", parts.unquoted)
    }
}
