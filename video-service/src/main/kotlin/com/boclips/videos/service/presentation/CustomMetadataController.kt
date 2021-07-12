package com.boclips.videos.service.presentation

import com.boclips.videos.service.presentation.converters.customMetadata.CustomMetadataFileValidator
import com.boclips.videos.service.presentation.exceptions.InvalidCustomMetadataCsvFile
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
class CustomMetadataController(private val customMetadataFileValidator: CustomMetadataFileValidator) {
    @PostMapping("/v1/custom-metadata", consumes = ["multipart/form-data"])
    fun postCustomMetadata(@RequestParam("file") file: MultipartFile?): ResponseEntity<SuccessResponse> {
        return when (val validationResult = customMetadataFileValidator.validate(file)) {
            is CustomMetadataValidated -> {
                ResponseEntity(SuccessResponse("Data has been successfully imported!"), HttpStatus.OK)
            }
            is CsvValidationMetadataError -> {
                throw InvalidCustomMetadataCsvFile(message = validationResult.getMessage())
            }
        }
    }
}
