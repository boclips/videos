package com.boclips.videos.service.testsupport

import com.boclips.videos.service.config.security.UserRoles
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder

fun MockHttpServletRequestBuilder.asTeacher(email: String = "teacher@gmail.com") =
    this.with(
        SecurityMockMvcRequestPostProcessors
            .user(email)
            .roles(
                UserRoles.VIEW_VIDEOS,
                UserRoles.DOWNLOAD_TRANSCRIPT,
                UserRoles.VIEW_COLLECTIONS,
                UserRoles.UPDATE_COLLECTIONS,
                UserRoles.DELETE_COLLECTIONS,
                UserRoles.INSERT_COLLECTIONS,
                UserRoles.INSERT_EVENTS
            )
    )

fun MockHttpServletRequestBuilder.asBoclipsEmployee(email: String = "teacher@boclips.com") =
    this.with(
        SecurityMockMvcRequestPostProcessors
            .user(email)
            .roles(
                UserRoles.VIEW_VIDEOS,
                UserRoles.DOWNLOAD_TRANSCRIPT,
                UserRoles.VIEW_DISABLED_VIDEOS,
                UserRoles.INSERT_EVENTS,
                UserRoles.UPDATE_VIDEOS,
                UserRoles.VIEW_COLLECTIONS,
                UserRoles.UPDATE_COLLECTIONS,
                UserRoles.DELETE_COLLECTIONS,
                UserRoles.INSERT_COLLECTIONS,
                UserRoles.CREATE_SUBJECT
            )
    )

fun MockHttpServletRequestBuilder.asOperator() =
    this.with(
        SecurityMockMvcRequestPostProcessors
            .user("operator")
            .roles(
                UserRoles.UPDATE_VIDEOS,
                UserRoles.REMOVE_VIDEOS,
                UserRoles.E2E,
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
            .roles(
                UserRoles.UPDATE_VIDEOS,
                UserRoles.VIEW_COLLECTIONS,
                UserRoles.VIEW_ANY_COLLECTION
            )
    )

fun MockHttpServletRequestBuilder.asVideoAnalyser() =
    this.with(
        SecurityMockMvcRequestPostProcessors
            .user("videoAnalyser")
            .roles(
                UserRoles.VIEW_VIDEOS,
                UserRoles.DOWNLOAD_VIDEOS
            )
    )

fun MockHttpServletRequestBuilder.asUserWithRoles(vararg roles: String) =
    this.with(
        SecurityMockMvcRequestPostProcessors
            .user("aRandomUser")
            .roles(*roles)
    )
