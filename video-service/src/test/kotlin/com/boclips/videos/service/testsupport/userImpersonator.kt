package com.boclips.videos.service.testsupport

import com.boclips.videos.service.config.security.UserRoles
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder

fun MockHttpServletRequestBuilder.asTeacher() =
        this.with(SecurityMockMvcRequestPostProcessors.user("teacher@gmail.com").roles(UserRoles.TEACHER))

fun MockHttpServletRequestBuilder.asBoclipsEmployee() =
        this.with(SecurityMockMvcRequestPostProcessors.user("teacher@boclips.com").roles(UserRoles.TEACHER))

fun MockHttpServletRequestBuilder.asOperator() =
        this.with(SecurityMockMvcRequestPostProcessors.user("operator").roles(UserRoles.REMOVE_VIDEOS, UserRoles.REBUILD_SEARCH_INDEX))

fun MockHttpServletRequestBuilder.asReporter() =
        this.with(SecurityMockMvcRequestPostProcessors.user("reporter").roles(UserRoles.REPORTING))

fun MockHttpServletRequestBuilder.asIngestor() =
        this.with(SecurityMockMvcRequestPostProcessors.user("ingestor").roles(UserRoles.INSERT_VIDEOS))
