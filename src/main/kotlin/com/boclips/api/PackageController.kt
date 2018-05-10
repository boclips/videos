package com.boclips.api

import com.boclips.api.infrastructure.toResourceOfResources
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class PackageController(val packageRepository: PackageRepository) {


    @GetMapping("/packages")
    fun getPackages() = packageRepository.findAll().toResourceOfResources()

}