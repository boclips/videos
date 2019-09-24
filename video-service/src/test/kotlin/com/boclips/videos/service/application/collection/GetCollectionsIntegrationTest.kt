package com.boclips.videos.service.application.collection

import com.boclips.users.client.model.contract.SelectedContentContract
import com.boclips.videos.service.application.getCurrentUserId
import com.boclips.videos.service.presentation.Projection
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.CollectionFilterFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class GetCollectionsIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var getCollections: GetCollections

    @Test
    fun `get collections`() {
        saveCollection(public = true)

        val collectionPage = getCollections(
            CollectionFilterFactory.sample(visibility = CollectionFilter.Visibility.PUBLIC, owner = null),
            Projection.list
        )

        assertThat(collectionPage.elements).hasSize(1)
    }

    @Test
    fun `get collections by contracts`() {
        val collectionId = saveCollection(public = true)

        userServiceClient.addContract(SelectedContentContract(listOf(collectionId.value)))

        val userContracts = userContractService.getContracts(getCurrentUserId().value)
        assertThat(userContracts).isNotEmpty

        val collectionPage = getCollections(
            CollectionFilterFactory.sample(visibility = CollectionFilter.Visibility.PUBLIC, owner = null),
            Projection.list
        )

        assertThat(collectionPage.elements).hasSize(1)
    }
}
