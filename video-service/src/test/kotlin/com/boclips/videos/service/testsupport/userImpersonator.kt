package com.boclips.videos.service.testsupport

import com.boclips.videos.service.config.security.UserRoles
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder

fun MockHttpServletRequestBuilder.asTeacher() =
    this.with(
        SecurityMockMvcRequestPostProcessors
            .user("teacher@gmail.com")
            .roles(
                UserRoles.VIEW_VIDEOS,
                UserRoles.INSERT_EVENTS
            )
    )

fun MockHttpServletRequestBuilder.asBoclipsEmployee() =
    this.with(
        SecurityMockMvcRequestPostProcessors
            .user("teacher@boclips.com")
            .roles(
                UserRoles.VIEW_VIDEOS,
                UserRoles.VIEW_DISABLED_VIDEOS,
                UserRoles.INSERT_EVENTS,
                UserRoles.UPDATE_VIDEOS
            )
    )

fun MockHttpServletRequestBuilder.asOperator() =
    this.with(
        SecurityMockMvcRequestPostProcessors
            .user("operator")
            .roles(
                UserRoles.UPDATE_VIDEOS,
                UserRoles.REMOVE_VIDEOS,
                UserRoles.REBUILD_SEARCH_INDEX
            )
    )

fun MockHttpServletRequestBuilder.asReporter() =
    this.with(
        SecurityMockMvcRequestPostProcessors
            .user("reporter")
            .roles(
                UserRoles.VIEW_EVENTS,
                UserRoles.INSERT_EVENTS
            )
    )

fun MockHttpServletRequestBuilder.asIngestor() =
    this.with(
        SecurityMockMvcRequestPostProcessors
            .user("ingestor")
            .roles(UserRoles.INSERT_VIDEOS)
    )

fun MockHttpServletRequestBuilder.asSubjectClassifier() =
    this.with(
        SecurityMockMvcRequestPostProcessors
            .user("subjectClassifier")
            .roles(UserRoles.UPDATE_VIDEOS)
    )

fun MockHttpServletRequestBuilder.asUserWithRoles(vararg roles: String) =
    this.with(
        SecurityMockMvcRequestPostProcessors
            .user("aRandomUser")
            .roles(*roles)
    )
