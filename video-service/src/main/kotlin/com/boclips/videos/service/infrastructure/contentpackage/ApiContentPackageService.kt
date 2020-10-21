package com.boclips.videos.service.infrastructure.contentpackage

import com.boclips.users.api.httpclient.ContentPackagesClient
import com.boclips.videos.service.application.accessrules.AccessRulesConverter
import com.boclips.videos.service.domain.model.AccessRules
import com.boclips.videos.service.domain.model.contentpackage.ContentPackageId
import com.boclips.videos.service.domain.service.user.ContentPackageService
import feign.FeignException
import mu.KLogging

class ApiContentPackageService(
    private val contentPackagesClient: ContentPackagesClient,
    private val accessRulesConverter: AccessRulesConverter
) : ContentPackageService {

    companion object : KLogging()

    override fun getAccessRules(id: ContentPackageId): AccessRules? =
        try {
            val resource = contentPackagesClient.find(id.value).accessRules
            AccessRules(
                videoAccess = accessRulesConverter.toVideoAccess(resource),
                collectionAccess = accessRulesConverter.toCollectionAccess(resource)
            )
        } catch (e: FeignException) {
            when (e.status()) {
                404 -> {
                    logger.info { "Content package not found for content package ID: ${id.value}" }
                }
                else -> logger.error(e) {
                    "Could not retrieve content package access rules " +
                        "for content package ID: ${id.value}"
                }
            }
            null
        }
}
