package com.boclips.videos.service.infrastructure.collection

import com.boclips.users.client.model.accessrule.AccessRule
import com.boclips.users.client.model.accessrule.SelectedCollectionsAccessRule
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.litote.kmongo.`in`

class MongoCollectionFilterContractAdapterTest {
    @Test
    fun `throws an illegal argument exception if given an access rule it doesn't understand`() {
        assertThatThrownBy { adapter.adapt(UnknownContractForTesting()) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("UnknownContractForTesting")
    }

    @Test
    fun `translates a SelectedContent access rule into an "id in ( )" clause`() {
        val firstId = ObjectId()
        val secondId = ObjectId()
        val thirdId = ObjectId()
        val selectedContent = SelectedCollectionsAccessRule().apply {
            collectionIds = listOf(firstId.toHexString(), secondId.toHexString(), thirdId.toHexString())
        }

        val filter = adapter.adapt(selectedContent)

        assertThat(filter.toString()).isEqualTo(
            (CollectionDocument::id `in` listOf(firstId, secondId, thirdId)).toString()
        )
    }

    inner class UnknownContractForTesting : AccessRule()

    private val adapter = MongoCollectionFilterAccessRuleAdapter()
}
