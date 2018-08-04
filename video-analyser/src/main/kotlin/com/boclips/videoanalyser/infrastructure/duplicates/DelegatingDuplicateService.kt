package com.boclips.videoanalyser.infrastructure.duplicates

import com.boclips.videoanalyser.domain.model.BoclipsVideo
import com.boclips.videoanalyser.domain.model.DuplicateVideo
import com.boclips.videoanalyser.domain.service.DuplicateService
import com.boclips.videoanalyser.infrastructure.BoclipsVideoRepository
import com.boclips.videoanalyser.infrastructure.duplicates.strategies.DuplicateStrategy
import mu.KLogging
import org.springframework.stereotype.Service

@Service
class DelegatingDuplicateService(
        val strategies: Set<DuplicateStrategy>,
        val boclipsVideoRepository: BoclipsVideoRepository,
        val remapperService: VideoRemapperService
) : DuplicateService {
    companion object : KLogging()

    override fun getDuplicates(): Set<DuplicateVideo> {
        val videos = boclipsVideoRepository.getAllVideos()

        val alreadyFoundDuplicates = mutableListOf<BoclipsVideo>()
        return strategies.flatMap {
            val latestDuplicates = it.findDuplicates(videos - alreadyFoundDuplicates)
            alreadyFoundDuplicates += latestDuplicates.flatMap { it.duplicates }
            latestDuplicates
        }.toSet()
    }

    override fun deleteDuplicates(duplicates: Set<DuplicateVideo>) {
        remapperService.disableIndexesBeforeRemapping()
        logger.info { "Indices disabled in Mongo to perform remapping" }
        var count = 0
        duplicates.forEach {
            remapperService.remapBasketsPlaylistsAndCollections(it)
            if (count % 1000 == 0) {
                logger.info { "Remapped $count/${duplicates.size}..." }
            }
            count++
        }
        val allDuplicates = duplicates.flatMap { it.duplicates }.toSet()
        remapperService.enableIndexesAfterRemapping()
        logger.info { "Indices restored in Mongo after successful remapping" }
        logger.info { "Deleting videos now from MySQL..." }
        boclipsVideoRepository.deleteVideos(allDuplicates)
    }

}