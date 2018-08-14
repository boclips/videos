package com.boclips.videos.infrastructure.packages

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PackageConfiguration {

    @Bean
    fun packageService(packageRepository: PackageRepository) = PackageServiceImpl(packageRepository)
}