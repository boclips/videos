package com.boclips.videos.service.testsupport

import com.boclips.videos.service.config.security.UserRoles
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder

fun MockHttpServletRequestBuilder.asTeacher() =
        this.with(SecurityMockMvcRequestPostProcessors.user("teacher").roles(UserRoles.TEACHER))

fun MockHttpServletRequestBuilder.asOperator() =
        this.with(SecurityMockMvcRequestPostProcessors.user("operator").roles(UserRoles.REMOVE_VIDEOS, UserRoles.REBUILD_SEARCH_INDEX))

fun MockHttpServletRequestBuilder.asReporter() =
        this.with(SecurityMockMvcRequestPostProcessors.user("reporter").roles(UserRoles.REPORTING))