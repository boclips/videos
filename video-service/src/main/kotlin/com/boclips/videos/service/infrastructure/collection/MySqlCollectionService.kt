package com.boclips.videos.service.infrastructure.collection

import com.boclips.videos.service.domain.model.UserId
import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.domain.service.AddVideoToCollection
import com.boclips.videos.service.domain.service.CollectionService
import com.boclips.videos.service.domain.service.CollectionUpdateCommand
import com.boclips.videos.service.domain.service.VideoService
import java.util.*

class MySqlCollectionService(
        private val collectionEntityRepository: CollectionEntityRepository,
        private val videoInCollectionEntityRepository: VideoInCollectionEntityRepository,
        private val videoService: VideoService
) : CollectionService {
    override fun create(owner: UserId): Collection {
        val collectionEntity = collectionEntityRepository.save(CollectionEntity(
                id = UUID.randomUUID().toString(),
                owner = owner.value,
                title = ""
        ))

        return convert(collectionEntity)
    }

    override fun update(id: CollectionId, updateCommand: CollectionUpdateCommand) {
        when (updateCommand) {
            is AddVideoToCollection -> addVideo(id, updateCommand.videoId)
            else -> throw Error("Not supported: $updateCommand")
        }
    }

    override fun getById(id: CollectionId): Collection {
        val collectionEntity = collectionEntityRepository.findById(id.value).orElseThrow { CollectionNotFoundException(id.value) }
        return getByEntity(collectionEntity)
    }

    override fun getByOwner(owner: UserId): List<Collection> {
        return collectionEntityRepository.findByOwner(owner.value).map { getByEntity(it) }
    }

    private fun getByEntity(collectionEntity: CollectionEntity): Collection {
        val videoInCollectionEntities = videoInCollectionEntityRepository.findByCollectionId(collectionEntity.id!!)
        val assetIds = videoInCollectionEntities.map { AssetId(it.videoId!!) }
        val videos = videoService.get(assetIds)
        return convert(collectionEntity).copy(videos = videos)
    }

    private fun addVideo(id: CollectionId, videoId: AssetId) {
        if (videoInCollectionEntityRepository.existsByCollectionIdAndVideoId(collectionId = id.value, videoId = videoId.value)) {
            return
        }
        videoInCollectionEntityRepository.save(VideoInCollectionEntity(collectionId = id.value, videoId = videoId.value))
    }

    private fun convert(collectionEntity: CollectionEntity): Collection {
        return Collection(
                id = CollectionId(collectionEntity.id!!),
                owner = UserId(value = collectionEntity.owner!!),
                title = collectionEntity.title!!,
                videos = emptyList()
        )
    }

}