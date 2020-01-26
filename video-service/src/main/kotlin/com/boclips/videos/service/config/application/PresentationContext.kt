package com.boclips.videos.service.config.application

import com.boclips.contentpartner.service.presentation.LegalRestrictionsLinkBuilder
import com.boclips.contentpartner.service.presentation.LegalRestrictionsToResourceConverter
import com.boclips.videos.service.application.collection.CollectionUpdatesConverter
import com.boclips.videos.service.domain.service.subject.SubjectRepository
import com.boclips.videos.service.domain.service.video.VideoService
import com.boclips.videos.service.presentation.converters.AgeRangeToResourceConverter
import com.boclips.videos.service.presentation.converters.AttachmentToResourceConverter
import com.boclips.videos.service.presentation.converters.CollectionResourceFactory
import com.boclips.videos.service.presentation.converters.PlaybackToResourceConverter
import com.boclips.videos.service.presentation.converters.TagConverter
import com.boclips.videos.service.presentation.converters.VideoToResourceConverter
import com.boclips.videos.service.presentation.hateoas.CollectionsLinkBuilder
import com.boclips.videos.service.presentation.hateoas.DisciplinesLinkBuilder
import com.boclips.videos.service.presentation.hateoas.EventsLinkBuilder
import com.boclips.videos.service.presentation.hateoas.PlaybacksLinkBuilder
import com.boclips.videos.service.presentation.hateoas.TagsLinkBuilder
import com.boclips.videos.service.presentation.hateoas.UriComponentsBuilderFactory
import com.boclips.videos.service.presentation.hateoas.VideosLinkBuilder
import com.boclips.videos.service.presentation.projections.RoleBasedProjectionResolver
import com.boclips.videos.service.presentation.projections.WithProjection
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.filter.ForwardedHeaderFilter

@Configuration
class PresentationContext(val videoService: VideoService) {

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
        return LegalRestrictionsToResourceConverter(legalRestrictionsLinkBuilder)
    }

    @Bean
    fun tagsConverter(tagsLinkBuilder: TagsLinkBuilder): TagConverter {
        return TagConverter(tagsLinkBuilder)
    }

    @Bean
    fun videoConverter(
        videosLinkBuilder: VideosLinkBuilder,
        playbackToResourceConverter: PlaybackToResourceConverter
    ): VideoToResourceConverter {
        return VideoToResourceConverter(videosLinkBuilder, playbackToResourceConverter)
    }

    @Bean
    fun playbackConverter(
        eventsLinkBuilder: EventsLinkBuilder,
        playbacksLinkBuilder: PlaybacksLinkBuilder
    ): PlaybackToResourceConverter {
        return PlaybackToResourceConverter(eventsLinkBuilder, playbacksLinkBuilder)
    }

    @Bean //TODO: collectionResourceFactory mixes different abstractions, address smell
    fun collectionResourceFactory(
        videosLinkBuilder: VideosLinkBuilder,
        playbackToResourceConverter: PlaybackToResourceConverter,
        attachmentsToResourceConverter: AttachmentToResourceConverter,
        collectionsLinkBuilder: CollectionsLinkBuilder
    ): CollectionResourceFactory {
        return CollectionResourceFactory(
            VideoToResourceConverter(
                videosLinkBuilder,
                playbackToResourceConverter
            ),
            attachmentsToResourceConverter,
            collectionsLinkBuilder,
            videoService
        )
    }
}
