package com.boclips.videos.service.config.application

import com.boclips.contentpartner.service.presentation.converters.LegalRestrictionsToResourceConverter
import com.boclips.contentpartner.service.presentation.hateoas.LegalRestrictionsLinkBuilder
import com.boclips.videos.service.application.GetAllCategories
import com.boclips.videos.service.application.channels.VideoChannelService
import com.boclips.videos.service.application.collection.CollectionUpdatesConverter
import com.boclips.videos.service.application.subject.GetSubjects
import com.boclips.videos.service.application.video.VideoRetrievalService
import com.boclips.videos.service.domain.service.subject.SubjectRepository
import com.boclips.videos.service.domain.service.taxonomy.CategoryService
import com.boclips.videos.service.domain.service.video.VideoRepository
import com.boclips.videos.service.presentation.converters.*
import com.boclips.videos.service.presentation.hateoas.*
import com.boclips.videos.service.presentation.projections.RoleBasedProjectionResolver
import com.boclips.videos.service.presentation.projections.WithProjection
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.filter.ForwardedHeaderFilter

@Configuration
class PresentationContext(val videoRetrievalService: VideoRetrievalService) {

    @Bean
    fun forwardedHeaderFilter(): FilterRegistrationBean<ForwardedHeaderFilter> {
        val filter = FilterRegistrationBean<ForwardedHeaderFilter>()
        filter.filter = ForwardedHeaderFilter()
        return filter
    }

    @Bean
    fun projectionResolver() = RoleBasedProjectionResolver()

    @Bean
    fun withProjection() = WithProjection(projectionResolver())

    @Bean
    fun disciplinesLinkBuilder(uriComponentsBuilderFactory: UriComponentsBuilderFactory): DisciplinesLinkBuilder {
        return DisciplinesLinkBuilder(uriComponentsBuilderFactory)
    }

    @Bean
    fun videosLinkBuilder(uriComponentsBuilderFactory: UriComponentsBuilderFactory): VideosLinkBuilder {
        return VideosLinkBuilder(uriComponentsBuilderFactory)
    }

    @Bean
    fun legalRestrictionsLinkBuilder(): LegalRestrictionsLinkBuilder {
        return LegalRestrictionsLinkBuilder()
    }

    @Bean
    fun ageRangeToResourceConverter(): AgeRangeToResourceConverter {
        return AgeRangeToResourceConverter()
    }

    @Bean
    fun collectionUpdatesConverter(subjectRepository: SubjectRepository): CollectionUpdatesConverter {
        return CollectionUpdatesConverter(subjectRepository) // TODO: should not depend on repository
    }

    @Bean
    fun legalRestrictionsConverter(legalRestrictionsLinkBuilder: LegalRestrictionsLinkBuilder): LegalRestrictionsToResourceConverter {
        return LegalRestrictionsToResourceConverter(
            legalRestrictionsLinkBuilder
        )
    }

    @Bean
    fun tagsConverter(tagsLinkBuilder: TagsLinkBuilder): TagConverter {
        return TagConverter(tagsLinkBuilder)
    }

    @Bean
    fun videoConverter(
        videosLinkBuilder: VideosLinkBuilder,
        playbackToResourceConverter: PlaybackToResourceConverter,
        attachmentsLinkBuilder: AttachmentsLinkBuilder,
        contentWarningLinkBuilder: ContentWarningLinkBuilder,
        videoChannelService: VideoChannelService,
        getSubjects: GetSubjects,
        categoryResourceConverter: CategoryResourceConverter,
        categoryService: CategoryService
    ): VideoToResourceConverter {

        return VideoToResourceConverter(
            videosLinkBuilder = videosLinkBuilder,
            playbackToResourceConverter = playbackToResourceConverter,
            attachmentToResourceConverter = AttachmentToResourceConverter(attachmentsLinkBuilder),
            contentWarningToResourceConverter = ContentWarningToResourceConverter(contentWarningLinkBuilder),
            videoChannelService = videoChannelService,
            getSubjects = getSubjects,
            categoryResourceConverter = categoryResourceConverter,
            categoryService = categoryService
        )
    }

    @Bean
    fun videoMetadataConverter(
        videosLinkBuilder: VideosLinkBuilder
    ): VideoMetadataConverter {
        return VideoMetadataConverter(videosLinkBuilder)
    }

    @Bean
    fun contentWarningLinkBuilder(): ContentWarningLinkBuilder = ContentWarningLinkBuilder()

    @Bean
    fun contentWarningToResourceConverter(contentWarningLinkBuilder: ContentWarningLinkBuilder): ContentWarningToResourceConverter {
        return ContentWarningToResourceConverter(contentWarningLinkBuilder)
    }

    @Bean
    fun playbackConverter(
        eventsLinkBuilder: EventsLinkBuilder,
        playbacksLinkBuilder: PlaybacksLinkBuilder
    ): PlaybackToResourceConverter {
        return PlaybackToResourceConverter(eventsLinkBuilder, playbacksLinkBuilder)
    }

    @Bean
    fun videoTaggingCsvFileValidator(
        getAllCategories: GetAllCategories,
        videoRepository: VideoRepository
    ): VideoTaggingCsvFileValidator {
        return VideoTaggingCsvFileValidator(getAllCategories, videoRepository)
    }

    @Bean // TODO: collectionResourceFactory mixes different abstractions, address smell. This is a mess.
    fun collectionResourceFactory(
        videosLinkBuilder: VideosLinkBuilder,
        playbackToResourceConverter: PlaybackToResourceConverter,
        attachmentsToResourceConverter: AttachmentToResourceConverter,
        contentWarningToResourceConverter: ContentWarningToResourceConverter,
        collectionsLinkBuilder: CollectionsLinkBuilder,
        videoChannelService: VideoChannelService,
        getSubjects: GetSubjects,
        categoryResourceConverter: CategoryResourceConverter,
        categoryService: CategoryService
    ): CollectionResourceConverter {
        return CollectionResourceConverter(
            VideoToResourceConverter(
                videosLinkBuilder,
                playbackToResourceConverter,
                attachmentsToResourceConverter,
                contentWarningToResourceConverter,
                videoChannelService,
                getSubjects,
                categoryResourceConverter,
                categoryService
            ),
            attachmentsToResourceConverter,
            collectionsLinkBuilder,
            videoRetrievalService
        )
    }
}
