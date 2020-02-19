package com.boclips.contentpartner.service.infrastructure

import com.boclips.contentpartner.service.config.properties.GcsProperties
import com.boclips.contentpartner.service.domain.model.SignedLinkProvider
import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.HttpMethod
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import java.net.URL
import java.util.HashMap
import java.util.UUID
import java.util.concurrent.TimeUnit

class GcsSignedLinkProvider(
    private val config: GcsProperties
) : SignedLinkProvider {
    override fun getLink(): URL {
        // mostly taken from https://cloud.google.com/storage/docs/access-control/signing-urls-with-helpers#storage-signed-url-object-java

        val storage: Storage = StorageOptions.newBuilder()
            .setProjectId(config.projectId).setCredentials(
                ServiceAccountCredentials.fromStream(config.secret.byteInputStream())
            ).build().service

        val objectName = UUID.randomUUID().toString()

        // Define Resource
        val blobInfo: BlobInfo = BlobInfo.newBuilder(BlobId.of(config.bucketName, objectName)).build()

        // Generate Signed URL
        val extensionHeaders: MutableMap<String, String> =
            HashMap()
        extensionHeaders["Content-Type"] = "application/octet-stream"

        return storage.signUrl(
            blobInfo,
            5,
            TimeUnit.MINUTES,
            Storage.SignUrlOption.httpMethod(HttpMethod.PUT),
            Storage.SignUrlOption.withExtHeaders(extensionHeaders),
            Storage.SignUrlOption.withV4Signature()
        )
    }
}
