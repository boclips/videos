package com.boclips.videos.service.domain.model.asset

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.reflect.full.memberProperties

class PartialVideoAssetTest {
    @Test
    fun `it encompasses all video asset attributes, except asset ID`() {
        val assetProps = VideoAsset::class.memberProperties.map { it.name }.filter { it != "assetId" }
        val partialAssetProps = PartialVideoAsset::class.memberProperties.map { it.name }

        assertThat(partialAssetProps).hasSameElementsAs(assetProps)
    }
}