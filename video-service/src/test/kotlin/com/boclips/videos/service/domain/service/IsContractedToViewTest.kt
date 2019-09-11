package com.boclips.videos.service.domain.service

import com.boclips.users.client.model.contract.SelectedContentContract
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class IsContractedToViewTest {
    @Test
    fun `returns true when there is a SelectedContent contract for a given collection`() {
        val collection = TestFactories.createCollection()
        val contracts = listOf(
            SelectedContentContract().apply {
                collectionIds = emptyList()
            },
            SelectedContentContract().apply {
                collectionIds = listOf("this is not the id we need", "and neither this")
            },
            SelectedContentContract().apply {
                collectionIds = listOf(collection.id.value)
            }
        )

        assertThat(isContractedToView(collection, contracts)).isTrue()
    }

    @Test
    fun `returns false when user has a SelectedContent contract but not for that collection`() {
        val collection = TestFactories.createCollection()
        val contracts = listOf(
            SelectedContentContract().apply {
                collectionIds = listOf("this is not the id we need", "and neither this")
            }
        )

        assertThat(isContractedToView(collection, contracts)).isFalse()
    }

    val isContractedToView = IsContractedToView()
}