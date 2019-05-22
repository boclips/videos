package com.boclips.videos.service.infrastructure.video.mongo

import com.boclips.videos.service.application.video.exceptions.VideoNotFoundException
import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.model.video.VideoFilter
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand.HideFromSearch
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand.MakeSearchable
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand.ReplaceAgeRange
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand.ReplaceDuration
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand.ReplaceKeywords
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand.ReplaceLanguage
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand.ReplacePlayback
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand.ReplaceSubjects
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand.ReplaceTopics
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand.ReplaceTranscript
import com.boclips.videos.service.infrastructure.DATABASE_NAME
import com.boclips.videos.service.infrastructure.video.mongo.converters.PlaybackConverter
import com.boclips.videos.service.infrastructure.video.mongo.converters.TopicDocumentConverter
import com.boclips.videos.service.infrastructure.video.mongo.converters.VideoDocumentConverter
import com.mongodb.MongoClient
import com.mongodb.client.model.Filters.and
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.UpdateOneModel
import mu.KLogging
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import org.litote.kmongo.`in`
import org.litote.kmongo.combine
import org.litote.kmongo.div
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.litote.kmongo.set
import java.util.Optional

class MongoVideoRepository(
    private val mongoClient: MongoClient
) : VideoRepository {
    companion object : KLogging() {

        const val collectionName = "videos"
    }

    override fun find(videoId: VideoId): Video? {
        val videoOrNull = getVideoCollection().findOne(VideoDocument::id eq ObjectId(videoId.value))
            ?.let(VideoDocumentConverter::toVideo)

        logger.info { "Found video ${videoId.value}" }

        return videoOrNull
    }

    override fun findAll(videoIds: List<VideoId>): List<Video> {
        val objectIds = videoIds.map { ObjectId(it.value) }

        val videos = getVideoCollection()
            .find(VideoDocument::id `in` objectIds)
            .map(VideoDocumentConverter::toVideo)
            .map { it.videoId to it }
            .toMap()

        return videoIds.mapNotNull { videoId -> videos[videoId] }
    }

    override fun streamAll(consumer: (Sequence<Video>) -> Unit) {
        val sequence = Sequence { getVideoCollection().find().iterator() }
            .map(VideoDocumentConverter::toVideo)

        consumer(sequence)
    }

    override fun streamAll(filter: VideoFilter, consumer: (Sequence<Video>) -> Unit) {
        val filterBson = when (filter) {
            is VideoFilter.ContentPartnerIs -> VideoDocument::source.div(SourceDocument::contentPartner).div(
                ContentPartnerDocument::name
            ) eq filter.contentPartnerName
            VideoFilter.IsSearchable -> VideoDocument::searchable eq true
            VideoFilter.IsYoutube -> VideoDocument::playback / PlaybackDocument::type eq PlaybackDocument.PLAYBACK_TYPE_YOUTUBE
            VideoFilter.IsKaltura -> VideoDocument::playback / PlaybackDocument::type eq PlaybackDocument.PLAYBACK_TYPE_KALTURA
        }

        val sequence = Sequence { getVideoCollection().find(filterBson).iterator() }
            .map(VideoDocumentConverter::toVideo)

        consumer(sequence)
    }

    override fun delete(videoId: VideoId) {
        val objectIdToBeDeleted = ObjectId(videoId.value)
        getVideoCollection().deleteOne(VideoDocument::id eq objectIdToBeDeleted)

        logger.info { "Deleted video ${videoId.value}" }
    }

    override fun create(video: Video): Video {
        val document = VideoDocumentConverter.toVideoDocument(video)

        getVideoCollection().insertOne(document)

        val createdVideo = find(video.videoId) ?: throw VideoNotFoundException(video.videoId)

        logger.info { "Created video ${createdVideo.videoId.value}" }
        return createdVideo
    }

    override fun update(command: VideoUpdateCommand): Video {
        val videoId = command.videoId
        getVideoCollection().updateOne(
            VideoDocument::id eq ObjectId(videoId.value),
            updatedOperation(command)
        )

        return find(videoId) ?: throw VideoNotFoundException(videoId)
    }

    override fun bulkUpdate(commands: List<VideoUpdateCommand>) {
        val updateDocs = commands.map { updateCommand ->
            UpdateOneModel<VideoDocument>(
                VideoDocument::id eq ObjectId(updateCommand.videoId.value),
                updatedOperation(updateCommand)
            )
        }

        val result = getVideoCollection().bulkWrite(updateDocs)
        logger.info("Bulk update: $result")
    }

    override fun existsVideoFromContentPartner(contentPartnerId: String, partnerVideoId: String): Boolean {
        val videoMatchingFilters = getVideoCollection()
            .find(
                and(
                    eq("source.contentPartner.name", contentPartnerId),
                    eq("source.videoReference", partnerVideoId)
                )
            )
            .first()

        return Optional.ofNullable(videoMatchingFilters).isPresent
    }

    override fun resolveAlias(alias: String): VideoId? {
        val videoId = getVideoCollection().findOne(eq("aliases", alias))
            ?.let { VideoId(it.id.toHexString()) }

        logger.info { "Attempted to resolve alias $alias to $videoId" }

        return videoId
    }

    private fun updatedOperation(updateCommand: VideoUpdateCommand): Bson {
        return when (updateCommand) {
            is ReplaceDuration -> set(
                VideoDocument::playback / PlaybackDocument::duration,
                updateCommand.duration.seconds.toInt()
            )
            is ReplaceSubjects -> set(VideoDocument::subjects, updateCommand.subjects.map { it.name })
            is MakeSearchable -> set(VideoDocument::searchable, true)
            is HideFromSearch -> set(VideoDocument::searchable, false)
            is ReplaceLanguage -> set(VideoDocument::language, updateCommand.language.toLanguageTag())
            is ReplaceTranscript -> set(VideoDocument::transcript, updateCommand.transcript)
            is ReplaceTopics -> set(VideoDocument::topics, updateCommand.topics.map(TopicDocumentConverter::toDocument))
            is ReplaceKeywords -> set(VideoDocument::keywords, updateCommand.keywords)
            is ReplacePlayback -> set(VideoDocument::playback, PlaybackConverter.toDocument(updateCommand.playback))
            is ReplaceAgeRange -> combine(
                set(
                    VideoDocument::ageRangeMin,
                    updateCommand.ageRange.min()
                ),
                set(
                    VideoDocument::ageRangeMax,
                    updateCommand.ageRange.max()
                )
            )
        }
    }

    private fun getVideoCollection() =
        mongoClient.getDatabase(DATABASE_NAME).getCollection<VideoDocument>(collectionName)
}
