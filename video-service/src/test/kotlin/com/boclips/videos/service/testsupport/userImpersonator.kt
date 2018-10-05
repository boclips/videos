package com.boclips.videos.service.testsupport

import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder

fun MockHttpServletRequestBuilder.asTeacher() =
        this.with(SecurityMockMvcRequestPostProcessors.user("teacher").roles("TEACHER"))