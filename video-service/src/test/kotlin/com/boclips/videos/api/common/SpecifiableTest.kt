package com.boclips.videos.api.common

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SpecifiableTest {
    @Test
    fun `read string`() {
        val json = """
            {
                "property": "real-value"
            }
        """.trimIndent()

        val specifiable = ObjectMapper().readValue(json, StringProperty::class.java)

        val value = when (specifiable.property) {
            is Specified -> specifiable.property.value
            is ExplicitlyNull -> throw IllegalStateException()
            null -> throw IllegalStateException()
        }

        assertThat(value).isEqualTo("real-value")
    }

    @Test
    fun `read list of strings`() {
        val json = """
            {
                "property": ["real-value"]
            }
        """.trimIndent()

        val specifiable = ObjectMapper().readValue(json, ListStringProperty::class.java)

        val value = when (specifiable.property) {
            is Specified -> specifiable.property.value
            is ExplicitlyNull -> throw IllegalStateException()
            null -> throw IllegalStateException()
        }

        assertThat(value).containsExactly("real-value")
    }

    @Test
    fun `reads explicit null`() {
        val json = """
            {
                "property": null
            }
        """.trimIndent()

        val specifiable = ObjectMapper().readValue(json, ListStringProperty::class.java)

        val value = when (specifiable.property) {
            is Specified -> throw IllegalStateException()
            is ExplicitlyNull -> true
            null -> throw IllegalStateException()
        }

        assertThat(value).isTrue()
    }

    @Test
    fun `reads implicit null`() {
        val json = """ {} """.trimIndent()

        val specifiable = ObjectMapper().readValue(json, ListStringProperty::class.java)

        val value = when (specifiable.property) {
            is Specified -> throw IllegalStateException()
            is ExplicitlyNull -> throw IllegalStateException()
            null -> true
        }

        assertThat(value).isTrue()
    }

    @Test
    fun `map`() {
        val json = """
            {
                "property": "3"
            }
        """.trimIndent()

        val specifiable = ObjectMapper().readValue(json, StringProperty::class.java)

        val value = specifiable.property?.map { it.toInt() }

        assertThat(value).isEqualTo(Specified(3))
    }

    data class StringProperty(val property: Specifiable<String>? = null)
    data class ListStringProperty(val property: Specifiable<List<String>>? = null)
}