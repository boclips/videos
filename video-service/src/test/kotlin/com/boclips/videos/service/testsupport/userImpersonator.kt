package com.boclips.videos.service.testsupport

import com.boclips.videos.service.config.security.UserRoles
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder

fun MockHttpServletRequestBuilder.asTeacher(email: String = "teacher@gmail.com") =
    this.with(
        SecurityMockMvcRequestPostProcessors
            .user(email)
            .roles(
                UserRoles.TEACHER,
                UserRoles.VIEW_VIDEOS,
                UserRoles.RATE_VIDEOS,
                UserRoles.TAG_VIDEOS,
                UserRoles.SHARE_VIDEOS,
                UserRoles.VIEW_DISCIPLINES,
                UserRoles.VIEW_TAGS,
                UserRoles.DOWNLOAD_TRANSCRIPT,
                UserRoles.VIEW_COLLECTIONS,
                UserRoles.UPDATE_COLLECTIONS,
                UserRoles.DELETE_COLLECTIONS,
                UserRoles.INSERT_COLLECTIONS,
                UserRoles.INSERT_EVENTS,
                UserRoles.VIEW_AGE_RANGES
            )
    )

fun MockHttpServletRequestBuilder.asApiUser(email: String = "api-user@gmail.com") =
    this.with(
        SecurityMockMvcRequestPostProcessors
            .user(email)
            .roles(
                UserRoles.VIEW_VIDEOS,
                UserRoles.VIEW_VIDEO_TYPES,
                UserRoles.VIEW_COLLECTIONS,
                UserRoles.VIEW_CONTENT_PARTNERS,
                UserRoles.API,
                UserRoles.VIEW_CONTENT_CATEGORIES
            )
    )

fun MockHttpServletRequestBuilder.asBoclipsEmployee(email: String = "employee@boclips.com") =
    this.with(
        SecurityMockMvcRequestPostProcessors
            .user(email)
            .roles(
                UserRoles.BACKOFFICE,
                UserRoles.VIEW_VIDEOS,
                UserRoles.DOWNLOAD_TRANSCRIPT,
                UserRoles.DOWNLOAD_VIDEO,
                UserRoles.VIEW_DISABLED_VIDEOS,
                UserRoles.INSERT_EVENTS,
                UserRoles.UPDATE_VIDEOS,
                UserRoles.VIEW_COLLECTIONS,
                UserRoles.UPDATE_COLLECTIONS,
                UserRoles.DELETE_COLLECTIONS,
                UserRoles.INSERT_COLLECTIONS,
                UserRoles.CREATE_SUBJECTS,
                UserRoles.DELETE_SUBJECTS,
                UserRoles.UPDATE_SUBJECTS,
                UserRoles.VIEW_TAGS,
                UserRoles.INSERT_TAGS,
                UserRoles.DELETE_TAGS,
                UserRoles.VIEW_CONTENT_PARTNERS,
                UserRoles.INSERT_CONTENT_PARTNERS,
                UserRoles.UPDATE_CONTENT_PARTNERS,
                UserRoles.VIEW_DISCIPLINES,
                UserRoles.INSERT_DISCIPLINES,
                UserRoles.UPDATE_DISCIPLINES,
                UserRoles.VIEW_DISTRIBUTION_METHODS,
                UserRoles.VIEW_ANY_COLLECTION,
                UserRoles.VIEW_AGE_RANGES,
                UserRoles.INSERT_AGE_RANGES,
                UserRoles.VIEW_ANY_COLLECTION,
                UserRoles.VIEW_MARKETING_STATUSES
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
            .roles(UserRoles.INSERT_VIDEOS, UserRoles.VIEW_VIDEOS)
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

fun MockHttpServletRequestBuilder.asUserWithRoles(vararg roles: String) =
    this.with(
        SecurityMockMvcRequestPostProcessors
            .user("aRandomUser")
            .roles(*roles)
    )
