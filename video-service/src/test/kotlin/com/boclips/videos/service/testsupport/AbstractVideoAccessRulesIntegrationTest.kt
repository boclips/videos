package com.boclips.videos.service.testsupport

import com.boclips.users.client.model.contract.SelectedVideosContract
import java.util.UUID

abstract class AbstractVideoAccessRulesIntegrationTest : AbstractSpringIntegrationTest() {
    fun createSelectedVideosContract(vararg contractedVideoIds: String) {
        userServiceClient.addContract(SelectedVideosContract().apply {
            name = UUID.randomUUID().toString()
            videoIds = contractedVideoIds.toList()
        })
    }
}