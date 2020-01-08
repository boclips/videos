package com.boclips.videos.service.config.application

import com.boclips.videos.service.domain.service.video.VideoService
import com.boclips.videos.service.presentation.converters.AttachmentToResourceConverter
import com.boclips.videos.service.presentation.converters.CollectionResourceFactory
import com.boclips.videos.service.presentation.converters.PlaybackToResourceConverter
import com.boclips.videos.service.presentation.converters.SubjectToResourceConverter
import com.boclips.videos.service.presentation.converters.VideoToResourceConverter
import com.boclips.videos.service.presentation.hateoas.AttachmentsLinkBuilder
import com.boclips.videos.service.presentation.hateoas.CollectionsLinkBuilder
import com.boclips.videos.service.presentation.hateoas.DisciplinesLinkBuilder
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

    @Bean //TODO: collectionResourceFactory mixes different abstractions, address smell
    fun collectionResourceFactory(
        videosLinkBuilder: VideosLinkBuilder,
        playbackToResourceConverter: PlaybackToResourceConverter,
        attachmentsLinkBuilder: AttachmentsLinkBuilder,
        collectionsLinkBuilder: CollectionsLinkBuilder
    ): CollectionResourceFactory {
        return CollectionResourceFactory(
            VideoToResourceConverter(
                videosLinkBuilder,
                playbackToResourceConverter
            ),
            SubjectToResourceConverter(),
            AttachmentToResourceConverter(
                attachmentsLinkBuilder
            ),
            collectionsLinkBuilder,
            videoService
        )
    }
}
