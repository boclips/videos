package com.boclips.contentpartner.service.infrastructure.signedlink.common

import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.HttpMethod
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import java.net.URL
import java.util.UUID
import java.util.concurrent.TimeUnit

class GcsSignedLinkUtil {
    companion object {
        private fun getStorage(options: SignedLinkOptions) =
            StorageOptions.newBuilder()
                .setProjectId(options.projectId).setCredentials(
                    ServiceAccountCredentials.fromStream(options.secret.byteInputStream())
                ).build().service

        fun signedPutLink(filename: String, options: SignedLinkOptions): URL {
            // mostly taken from https://cloud.google.com/storage/docs/access-control/signing-urls-with-helpers#storage-signed-url-object-java

            val storage = getStorage(options)

            val objectName = UUID.randomUUID().toString()

            // Define Resource
            val blobInfo: BlobInfo =
                BlobInfo
                    .newBuilder(BlobId.of(options.bucketName, objectName))
                    .build()

            // Generate Signed URL
            val extensionHeaders: MutableMap<String, String> =
                HashMap()
            extensionHeaders["Content-Type"] = "application/octet-stream"
            extensionHeaders["Content-Disposition"] =
                "attachment; filename=\"${filename}\""

            return storage.signUrl(
                blobInfo,
                5,
                TimeUnit.MINUTES,
                Storage.SignUrlOption.httpMethod(HttpMethod.PUT),
                Storage.SignUrlOption.withExtHeaders(extensionHeaders),
                Storage.SignUrlOption.withV4Signature()
            )
        }

        fun signedGetLink(link: URL, options: SignedLinkOptions): URL {
            val storage = getStorage(options)

            // Define Resource
            val blobInfo: BlobInfo =
                BlobInfo
                    .newBuilder(BlobId.of(options.bucketName, link.toString().split('/').last()))
                    .build()

            return storage.signUrl(
                blobInfo,
                60,
                TimeUnit.MINUTES,
                Storage.SignUrlOption.httpMethod(HttpMethod.GET),
                Storage.SignUrlOption.withV4Signature()
            )
        }
    }
}

data class SignedLinkOptions(
    val bucketName: String,
    val secret: String,
    val projectId: String
)