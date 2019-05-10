package com.boclips.videos.service.infrastructure.video.mongo

import com.boclips.videos.service.application.video.exceptions.VideoAssetNotFoundException
import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.asset.VideoAsset
import com.boclips.videos.service.domain.model.asset.VideoAssetFilter
import com.boclips.videos.service.domain.model.asset.VideoAssetRepository
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand.HideFromSearch
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand.MakeSearchable
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand.ReplaceDuration
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand.ReplaceKeywords
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand.ReplaceLanguage
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand.ReplacePlayback
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand.ReplaceSubjects
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand.ReplaceTopics
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand.ReplaceTranscript
import com.boclips.videos.service.infrastructure.DATABASE_NAME
import com.mongodb.MongoClient
import com.mongodb.client.model.Filters.and
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.UpdateOneModel
import mu.KLogging
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import org.litote.kmongo.`in`
import org.litote.kmongo.div
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.litote.kmongo.set
import java.util.Optional

class MongoVideoAssetRepository(
    private val mongoClient: MongoClient
) : VideoAssetRepository {
    companion object : KLogging() {

        const val collectionName = "videos"
    }

    override fun find(assetId: AssetId): VideoAsset? {
        val videoAssetOrNull = getVideoCollection().findOne(VideoDocument::id eq ObjectId(assetId.value))
            ?.let(VideoDocumentConverter::toVideoAsset)

        logger.info { "Found ${assetId.value}" }

        return videoAssetOrNull
    }

    override fun findAll(assetIds: List<AssetId>): List<VideoAsset> {
        val objectIds = assetIds.map { ObjectId(it.value) }

        val assets = getVideoCollection()
            .find(VideoDocument::id `in` objectIds)
            .map(VideoDocumentConverter::toVideoAsset)
            .map { it.assetId to it }
            .toMap()

        return assetIds.mapNotNull { assetId -> assets[assetId] }
    }

    override fun streamAll(filter: VideoAssetFilter, consumer: (Sequence<VideoAsset>) -> Unit) {
        val filterBson = when (filter) {
            is VideoAssetFilter.ContentPartnerIs -> VideoDocument::source.div(SourceDocument::contentPartner).div(
                ContentPartnerDocument::name
            ) eq filter.contentPartnerName
            VideoAssetFilter.IsSearchable -> VideoDocument::searchable eq true
        }

        val sequence = Sequence { getVideoCollection().find(filterBson).iterator() }
            .map(VideoDocumentConverter::toVideoAsset)

        consumer(sequence)
    }

    override fun delete(assetId: AssetId) {
        val objectIdToBeDeleted = ObjectId(assetId.value)
        getVideoCollection().deleteOne(VideoDocument::id eq objectIdToBeDeleted)

        logger.info { "Deleted video ${assetId.value}" }
    }

    override fun create(videoAsset: VideoAsset): VideoAsset {
        val document = VideoDocumentConverter.toVideoDocument(videoAsset)

        getVideoCollection().insertOne(document)

        val createdVideoAsset = find(videoAsset.assetId) ?: throw VideoAssetNotFoundException(videoAsset.assetId)

        logger.info { "Created video ${createdVideoAsset.assetId.value}" }
        return createdVideoAsset
    }

    override fun update(command: VideoUpdateCommand): VideoAsset {
        val assetId = command.assetId
        getVideoCollection().updateOne(
            VideoDocument::id eq ObjectId(assetId.value),
            updatedOperation(command)
        )

        return find(assetId) ?: throw VideoAssetNotFoundException(assetId)
    }

    override fun bulkUpdate(commands: List<VideoUpdateCommand>) {
        val updateDocs = commands.map { updateCommand ->
            UpdateOneModel<VideoDocument>(
                VideoDocument::id eq ObjectId(updateCommand.assetId.value),
                updatedOperation(updateCommand)
            )
        }

        val result = getVideoCollection().bulkWrite(updateDocs)
        logger.info("Bulk update: $result")
    }

    override fun existsVideoFromContentPartner(contentPartnerId: String, partnerVideoId: String): Boolean {
        val assetMatchingFilters = getVideoCollection()
            .find(
                and(
                    eq("source.contentPartner.name", contentPartnerId),
                    eq("source.videoReference", partnerVideoId)
                )
            )
            .first()

        return Optional.ofNullable(assetMatchingFilters).isPresent
    }

    override fun resolveAlias(alias: String): AssetId? {
        val assetId = getVideoCollection().findOne(eq("aliases", alias))
            ?.let { AssetId(it.id.toHexString()) }

        logger.info { "Attempted to resolve alias $alias to $assetId" }

        return assetId
    }

    private fun updatedOperation(updateCommand: VideoUpdateCommand): Bson {
        return when (updateCommand) {
            is ReplaceDuration -> set(
                VideoDocument::durationSeconds,
                updateCommand.duration.seconds.toInt()
            )
            is ReplaceSubjects -> set(
                VideoDocument::subjects,
                updateCommand.subjects.map { it.name })
            is MakeSearchable -> set(VideoDocument::searchable, true)
            is HideFromSearch -> set(VideoDocument::searchable, false)
            is ReplaceLanguage -> set(
                VideoDocument::language,
                updateCommand.language.toLanguageTag()
            )
            is ReplaceTranscript -> set(VideoDocument::transcript, updateCommand.transcript)
            is ReplaceTopics -> set(
                VideoDocument::topics,
                updateCommand.topics.map(TopicDocumentConverter::toDocument)
            )
            is ReplaceKeywords -> set(
                VideoDocument::keywords,
                updateCommand.keywords
            )
            is ReplacePlayback -> set(
                VideoDocument::playback, VideoDocumentConverter.toPlaybackDocument(
                    updateCommand.playback,
                    updateCommand.playback.id
                )
            )
        }
    }

    private fun getVideoCollection() =
        mongoClient.getDatabase(DATABASE_NAME).getCollection<VideoDocument>(collectionName)
}
