package com.boclips.videos.service.application

import com.boclips.search.service.domain.ProgressNotifier
import com.boclips.videos.service.application.video.RebuildSearchIndex
import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.asset.VideoAssetRepository
import com.boclips.videos.service.infrastructure.video.mongo.MongoVideoAssetRepository
import mu.KLogging
import org.springframework.scheduling.annotation.Async
import java.util.concurrent.CompletableFuture

open class MigrateToMongo(
        private val mysqlVideoAssetRepository: VideoAssetRepository,
        private val mongoVideoAssetRepository: MongoVideoAssetRepository
) {
    companion object: KLogging()

    @Async
    open fun execute(notifier: ProgressNotifier? = null): CompletableFuture<Unit> {
        try {
            mysqlVideoAssetRepository.streamAll { videos ->
                videos.windowed(size = 1000, step = 1000, partialWindows = true).forEachIndexed { index, batch ->
                    notifier?.send("Migrating batch $index")
                    batch.forEach { video ->
                        val mysqlId = video.assetId
                        val mongoId = AssetId(value = "", alias = mysqlId.value)
                        mongoVideoAssetRepository.create(video.copy(assetId = mongoId))
                    }
                }
            }
        } catch (e: Exception) {
            RebuildSearchIndex.logger.error("Error migrating data", e)
        }

        RebuildSearchIndex.logger.info("Data migration done")
        return CompletableFuture.completedFuture(null)
    }
}
