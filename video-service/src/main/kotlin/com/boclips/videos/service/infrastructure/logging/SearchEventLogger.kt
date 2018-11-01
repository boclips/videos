package com.boclips.videos.service.infrastructure.logging

import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut

@Aspect
class SearchLoggingPointcuts {

    @Pointcut("within(com.boclips.videos.service..*)")
    fun inVideoService() {
    }

    @Pointcut("@annotation(com.boclips.videos.service.infrastructure.logging.SearchLogging)")
    fun annotatedWithSearchLogging() {

    }

    @Pointcut("inVideoService() && annotatedWithSearchLogging()")
    fun searchLoggingAnnotation() {
    }

}
