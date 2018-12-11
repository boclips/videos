package com.boclips.videos.service.client.spring

import org.springframework.context.annotation.Import

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
@Import(VideoServiceClientConfig::class)
annotation class EnableVideoServiceClient