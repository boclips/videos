package com.boclips.videos.service.infrastructure.video

import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.infrastructure.video.mongo.MongoVideoAssetRepository
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class MongoVideoAssetRepositoryIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var mongoVideoRepository: MongoVideoAssetRepository

    @Test
    fun `create and find a video`() {
        val id = "5ba8e657042ade0001d563fc"
        val originalAsset = TestFactories.createVideoAsset(videoId = id)

        mongoVideoRepository.create(originalAsset)
        val retrievedAsset = mongoVideoRepository.find(AssetId(id))!!

        assertThat(retrievedAsset).isEqualToComparingFieldByFieldRecursively(originalAsset)
    }

    @Test
    fun `find video when does not exist`() {
        assertThat(mongoVideoRepository.find(AssetId(ObjectId().toHexString()))).isNull()
    }

    @Test
    fun `lookup videos by ids maintains video order`() {
        val id1 = ObjectId().toHexString()
        val id2 = ObjectId().toHexString()
        val id3 = ObjectId().toHexString()

        mongoVideoRepository.create(TestFactories.createVideoAsset(videoId = id1))
        mongoVideoRepository.create(TestFactories.createVideoAsset(videoId = id2))
        mongoVideoRepository.create(TestFactories.createVideoAsset(videoId = id3))

        val videos = mongoVideoRepository.findAll(listOf(
                AssetId(id2),
                AssetId(id1),
                AssetId(id3)
        ))

        assertThat(videos.map { it.assetId.value }).containsExactly(id2, id1, id3)
    }

    @Test
    fun `delete a video`() {
        val id = "5ba8e657042ade0001d563fc"
        val originalAsset = TestFactories.createVideoAsset(videoId = id)

        mongoVideoRepository.create(originalAsset)
        mongoVideoRepository.delete(AssetId(id))

        assertThat(mongoVideoRepository.find(AssetId(id))).isNull()
    }
}