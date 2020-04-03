package com.boclips.contentpartner.service.infrastructure.signedlink

import com.boclips.contentpartner.service.config.properties.GcsProperties
import com.boclips.contentpartner.service.domain.model.SignedLinkProvider
import com.boclips.contentpartner.service.infrastructure.signedlink.common.GcsSignedLinkUtil
import com.boclips.contentpartner.service.infrastructure.signedlink.common.SignedLinkOptions
import java.net.URL

class ContentPartnerContractSignedLinkProvider(
    private val config: GcsProperties
) : SignedLinkProvider {
    override fun getLink(filename: String): URL =
        GcsSignedLinkUtil.getLink(
            SignedLinkOptions(
                filename = filename,
                bucketName = config.contractBucketName,
                secret = config.contractSecret,
                projectId = config.projectId
            )
        )
}