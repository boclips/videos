package com.boclips.contentpartner.service.infrastructure.signedlink

import com.boclips.contentpartner.service.config.properties.GcsProperties
import com.boclips.contentpartner.service.domain.model.SignedLinkProvider
import com.boclips.contentpartner.service.infrastructure.signedlink.common.GcsSignedLinkUtil
import com.boclips.contentpartner.service.infrastructure.signedlink.common.SignedLinkOptions
import java.net.URL

class ContentPartnerContractsSignedLinkProvider(
    private val config: GcsProperties
) : SignedLinkProvider {
    override fun signedPutLink(filename: String): URL =
        GcsSignedLinkUtil.signedPutLink(
            filename,
            SignedLinkOptions(
                bucketName = config.contractBucketName,
                secret = config.contractSecret,
                projectId = config.projectId
            )
        )

    override fun signedGetLink(link: URL): URL =
        GcsSignedLinkUtil.signedGetLink(
            link,
            SignedLinkOptions(
                bucketName = config.contractBucketName,
                secret = config.contractSecret,
                projectId = config.projectId
            )
        )
}
