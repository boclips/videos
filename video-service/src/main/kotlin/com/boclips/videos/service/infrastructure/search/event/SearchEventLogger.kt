package com.boclips.videos.service.infrastructure.search.event

import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut

@Aspect
class SearchLoggingPointcuts {

    @Pointcut("within(com.boclips.videos.service..*)")
    fun inVideoService() {
    }

    @Pointcut("@annotation(com.boclips.videos.service.infrastructure.search.event.SearchLogging)")
    fun annotatedWithSearchLogging() {

    }

    @Pointcut("inVideoService() && annotatedWithSearchLogging()")
    fun searchLoggingAnnotation() {
    }

}
