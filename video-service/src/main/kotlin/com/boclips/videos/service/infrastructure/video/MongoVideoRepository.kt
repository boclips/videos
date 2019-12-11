package com.boclips.videos.service.infrastructure.video

import com.boclips.contentpartner.service.domain.model.ContentPartnerId
import com.boclips.videos.service.application.video.exceptions.VideoNotFoundException
import com.boclips.videos.service.config.properties.BatchProcessingConfig
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.VideoFilter
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand.AddRating
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand.ReplaceAgeRange
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand.ReplaceContentPartner
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand.ReplaceDescription
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand.ReplaceDistributionMethods
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand.ReplaceDuration
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand.ReplaceKeywords
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand.ReplaceLanguage
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand.ReplaceLegalRestrictions
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand.ReplacePlayback
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand.ReplacePromoted
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand.ReplaceSubjects
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand.ReplaceTag
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand.ReplaceTitle
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand.ReplaceTopics
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand.ReplaceTranscript
import com.boclips.videos.service.infrastructure.DATABASE_NAME
import com.boclips.videos.service.infrastructure.subject.SubjectDocument
import com.boclips.videos.service.infrastructure.subject.SubjectDocumentConverter
import com.boclips.videos.service.infrastructure.video.converters.ContentPartnerDocumentConverter
import com.boclips.videos.service.infrastructure.video.converters.DistributionMethodDocumentConverter
import com.boclips.videos.service.infrastructure.video.converters.PlaybackConverter
import com.boclips.videos.service.infrastructure.video.converters.TopicDocumentConverter
import com.boclips.videos.service.infrastructure.video.converters.UserRatingDocumentConverter
import com.boclips.videos.service.infrastructure.video.converters.UserTagDocumentConverter
import com.boclips.videos.service.infrastructure.video.converters.VideoDocumentConverter
import com.mongodb.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters.and
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Filters.or
import com.mongodb.client.model.UpdateOneModel
import mu.KLogging
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import org.litote.kmongo.`in`
import org.litote.kmongo.addToSet
import org.litote.kmongo.combine
import org.litote.kmongo.contains
import org.litote.kmongo.div
import org.litote.kmongo.elemMatch
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.litote.kmongo.pullByFilter
import org.litote.kmongo.push
import org.litote.kmongo.set
import java.time.Instant
import java.util.Optional

class MongoVideoRepository(private val mongoClient: MongoClient, val batchProcessingConfig: BatchProcessingConfig) :
    VideoRepository {

    companion object : KLogging() {

        const val collectionName = "videos"
    }

    override fun find(videoId: VideoId): Video? {
        return getVideoCollection().findOne(VideoDocument::id eq ObjectId(videoId.value))
            ?.let(VideoDocumentConverter::toVideo)
    }

    override fun findAll(videoIds: List<VideoId>): List<Video> {
        val uniqueVideoIds = videoIds.distinct()
        val uniqueObjectIds = uniqueVideoIds.map { it.value }.distinct().map { ObjectId(it) }

        val videos = getVideoCollection()
            .find(VideoDocument::id `in` uniqueObjectIds)
            .map(VideoDocumentConverter::toVideo)
            .map { it.videoId to it }
            .toMap()

        return uniqueVideoIds.mapNotNull { videoId -> videos[videoId] }
    }

    override fun findByContentPartnerName(contentPartnerName: String): List<Video> {
        return getVideoCollection()
            .find((VideoDocument::source / SourceDocument::contentPartner / ContentPartnerDocument::name) eq contentPartnerName)
            .map(VideoDocumentConverter::toVideo)
            .toList()
    }

    override fun findByContentPartnerId(contentPartnerId: ContentPartnerId): List<Video> {
        val bson =
            (VideoDocument::source / SourceDocument::contentPartner / ContentPartnerDocument::id) eq
                ObjectId(contentPartnerId.value)

        return getVideoCollection()
            .find(bson)
            .map { VideoDocumentConverter.toVideo(it) }
            .toList()
    }

    override fun streamAll(consumer: (Sequence<Video>) -> Unit) {
        val sequence = Sequence { getVideoCollection().find().iterator() }
            .map(VideoDocumentConverter::toVideo)

        consumer(sequence)
    }

    override fun streamAll(filter: VideoFilter, consumer: (Sequence<Video>) -> Unit) {
        val filterBson = when (filter) {
            is VideoFilter.ContentPartnerNameIs -> VideoDocument::source / SourceDocument::contentPartner / ContentPartnerDocument::name eq filter.contentPartnerName
            is VideoFilter.ContentPartnerIdIs -> VideoDocument::source / SourceDocument::contentPartner / ContentPartnerDocument::id eq ObjectId(
                filter.contentPartnerId.value
            )
            is VideoFilter.ContentTypeIs -> VideoDocument::contentType eq filter.type.name
            VideoFilter.IsYoutube -> VideoDocument::playback / PlaybackDocument::type eq PlaybackDocument.PLAYBACK_TYPE_YOUTUBE
            VideoFilter.IsKaltura -> VideoDocument::playback / PlaybackDocument::type eq PlaybackDocument.PLAYBACK_TYPE_KALTURA
            VideoFilter.IsDownloadable -> or(
                VideoDocument::distributionMethods eq null,
                VideoDocument::distributionMethods contains DistributionMethodDocument(
                    DistributionMethodDocument.DELIVERY_METHOD_DOWNLOAD
                )
            )
            VideoFilter.IsStreamable -> or(
                VideoDocument::distributionMethods eq null,
                VideoDocument::distributionMethods contains DistributionMethodDocument(
                    DistributionMethodDocument.DELIVERY_METHOD_STREAM
                )
            )
            is VideoFilter.HasSubjectId -> VideoDocument::subjects elemMatch (SubjectDocument::id eq ObjectId(filter.subjectId.value))
        }

        val sequence = Sequence {
            getVideoCollection()
                .find(filterBson)
                .noCursorTimeout(true)
                .iterator()
        }
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
        // https://jira.mongodb.org/browse/SERVER-1050
        // We can consolidate this into 1 command when we upgrade to mongo 4.2, but right now it is BETA in Atlas
        // In the meanwhile we need 2 queries, therefore, we cannot use the commands to build criteria
        // I do not see the point of major refactor as it'll be feasible in a few weeks/months and anyway the
        // code inconsistency is isolated where the root issue is (mongodb persistence layer)
        if (command is AddRating) {
            addRating(command)
        } else {
            getVideoCollection().updateOne(
                VideoDocument::id eq ObjectId(videoId.value),
                updatedOperation(command)
            )
        }

        return find(videoId) ?: throw VideoNotFoundException(videoId)
    }

    override fun bulkUpdate(commands: List<VideoUpdateCommand>): List<Video> {
        if (commands.isEmpty()) return emptyList()

        val updateDocs = commands.map { updateCommand ->
            UpdateOneModel<VideoDocument>(
                VideoDocument::id eq ObjectId(updateCommand.videoId.value),
                updatedOperation(updateCommand)
            )
        }

        val result = getVideoCollection().bulkWrite(updateDocs)
        logger.info("Bulk video update: $result")

        return findAll(commands.map { it.videoId })
    }

    override fun streamUpdate(consumer: (List<Video>) -> List<VideoUpdateCommand>) {
        streamAll { videos ->
            videos.windowed(
                size = batchProcessingConfig.videoBatchSize,
                step = batchProcessingConfig.videoBatchSize,
                partialWindows = true
            ).forEach { windowedVideos ->
                bulkUpdate(commands = consumer(windowedVideos))
            }
        }
    }

    override fun streamUpdate(filter: VideoFilter, consumer: (List<Video>) -> List<VideoUpdateCommand>) {
        streamAll(filter) { videos ->
            videos.windowed(
                size = batchProcessingConfig.videoBatchSize,
                step = batchProcessingConfig.videoBatchSize,
                partialWindows = true
            ).forEachIndexed { index, windowedVideos ->
                logger.info { "Starting update batch: $index" }
                val updateCommands = consumer(windowedVideos)
                val updatedVideos = bulkUpdate(commands = updateCommands)
                logger.info { "Updated ${updatedVideos.size} videos" }
            }
        }
    }

    override fun existsVideoFromContentPartnerName(contentPartnerName: String, partnerVideoId: String): Boolean {
        val videoMatchingFilters = getVideoCollection()
            .find(
                and(
                    eq("source.contentPartner.name", contentPartnerName),
                    eq("source.videoReference", partnerVideoId)
                )
            )
            .first()

        return Optional.ofNullable(videoMatchingFilters).isPresent
    }

    override fun existsVideoFromContentPartnerId(contentPartnerId: ContentPartnerId, partnerVideoId: String): Boolean {
        if (!ObjectId.isValid(contentPartnerId.value)) {
            return false
        }

        val videoMatchingFilters = getVideoCollection()
            .find(
                and(
                    VideoDocument::source / SourceDocument::contentPartner / ContentPartnerDocument::id eq ObjectId(
                        contentPartnerId.value
                    ),
                    VideoDocument::source / SourceDocument::videoReference eq partnerVideoId
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
            is ReplaceSubjects -> set(
                VideoDocument::subjects,
                updateCommand.subjects.map(SubjectDocumentConverter::toSubjectDocument)
            )
            is ReplaceLanguage -> set(VideoDocument::language, updateCommand.language.toLanguageTag())
            is ReplaceTranscript -> set(VideoDocument::transcript, updateCommand.transcript)
            is ReplaceTopics -> set(
                VideoDocument::topics,
                updateCommand.eventBus.map(TopicDocumentConverter::toDocument)
            )
            is ReplaceKeywords -> set(VideoDocument::keywords, updateCommand.keywords)
            is ReplacePlayback -> set(VideoDocument::playback, PlaybackConverter.toDocument(updateCommand.playback))
            is AddRating -> throw IllegalStateException("cannot add rating in a single query")
            is ReplaceTag -> set(
                VideoDocument::tags,
                listOf(UserTagDocumentConverter.toDocument(updateCommand.tag))
            )
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
            is ReplaceContentPartner -> {
                val contentPartnerDocument =
                    ContentPartnerDocumentConverter.toContentPartnerDocument(updateCommand.contentPartner)
                set(
                    VideoDocument::source / SourceDocument::contentPartner,
                    contentPartnerDocument.copy(lastModified = Instant.now())
                )
            }
            is ReplaceDistributionMethods -> set(
                VideoDocument::distributionMethods,
                updateCommand.distributionMethods.map(DistributionMethodDocumentConverter::toDocument).toSet()
            )
            is ReplaceTitle -> set(VideoDocument::title, updateCommand.title)
            is ReplaceDescription -> set(VideoDocument::description, updateCommand.description)
            is ReplaceLegalRestrictions -> set(VideoDocument::legalRestrictions, updateCommand.legalRestrictions.text)
            is ReplacePromoted -> set(VideoDocument::promoted, updateCommand.promoted)
            is VideoUpdateCommand.AddShareCode -> addToSet(VideoDocument::shareCodes, updateCommand.shareCode)
        }
    }

    private fun addRating(addRating: AddRating) {
        getVideoCollection().updateOne(
            VideoDocument::id eq ObjectId(addRating.videoId.value),
            pullByFilter(
                VideoDocument::rating,
                UserRatingDocument::userId eq addRating.rating.userId.value
            )
        )
        getVideoCollection().updateOne(
            VideoDocument::id eq ObjectId(addRating.videoId.value),
            push(
                VideoDocument::rating,
                UserRatingDocumentConverter.toDocument(addRating.rating)
            )
        )
    }

    private fun getVideoCollection(): MongoCollection<VideoDocument> {
        return mongoClient.getDatabase(DATABASE_NAME).getCollection<VideoDocument>(collectionName)
    }
}
