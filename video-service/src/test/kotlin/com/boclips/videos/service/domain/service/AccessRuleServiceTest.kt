package com.boclips.videos.service.domain.service

import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AccessRuleServiceTest {
    @Test
    fun `returns true when there is a SelectedContent contract for a given collection`() {
        val collection = TestFactories.createCollection()
        val accessRule = AccessRule(CollectionAccessRule.specificIds(listOf(collection.id)))

        assertThat(accessRule.allowsAccessTo(collection)).isTrue()
    }

    @Test
    fun `returns false when user has a SelectedContent contract but not for that collection`() {
        val collection = TestFactories.createCollection()
        val accessRule = AccessRule(CollectionAccessRule.specificIds(listOf(CollectionId("some--random-id"))))

        assertThat(accessRule.allowsAccessTo(collection)).isFalse()
    }
}
