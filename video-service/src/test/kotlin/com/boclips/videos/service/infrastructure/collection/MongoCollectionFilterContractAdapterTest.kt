package com.boclips.videos.service.infrastructure.collection

import com.boclips.users.api.response.accessrule.AccessRuleResource
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.litote.kmongo.`in`

class MongoCollectionFilterContractAdapterTest {
    @Test
    fun `throws an illegal argument exception if given an access rule it doesn't understand`() {
        assertThatThrownBy { adapter.adapt(unknownContractForTesting) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("AccessRuleResource\$ExcludedVideoTypes")
    }

    @Test
    fun `translates a IncludedContent access rule into an "id in ( )" clause`() {
        val firstId = ObjectId()
        val secondId = ObjectId()
        val thirdId = ObjectId()
        val selectedContent = AccessRuleResource.IncludedCollections(
            name = "included collections",
            collectionIds = listOf(firstId.toHexString(), secondId.toHexString(), thirdId.toHexString())
        )

        val filter = adapter.adapt(selectedContent)

        assertThat(filter.toString()).isEqualTo(
            (CollectionDocument::id `in` listOf(firstId, secondId, thirdId)).toString()
        )
    }

    private val unknownContractForTesting =
        AccessRuleResource.ExcludedVideoTypes(
            name = "not collection",
            videoTypes = emptyList()
        )

    private val adapter = MongoCollectionFilterAccessRuleAdapter()
}
