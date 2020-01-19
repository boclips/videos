package com.boclips.contentpartner.service.presentation

import com.boclips.contentpartner.service.application.CreateLegalRestrictions
import com.boclips.contentpartner.service.application.FindAllLegalRestrictions
import com.boclips.contentpartner.service.application.FindLegalRestrictions
import com.boclips.security.utils.UserExtractor.getIfHasRole
import com.boclips.videos.api.response.contentpartner.LegalRestrictionsResource
import com.boclips.videos.service.config.security.UserRoles
import org.springframework.hateoas.CollectionModel
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.Link
import org.springframework.hateoas.server.mvc.ControllerLinkBuilder.methodOn
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/legal-restrictions")
class LegalRestrictionsController(
    private val createLegalRestrictions: CreateLegalRestrictions,
    private val findLegalRestrictions: FindLegalRestrictions,
    private val findAllLegalRestrictions: FindAllLegalRestrictions
) : BaseController() {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun post(@RequestParam text: String?): EntityModel<LegalRestrictionsResource> {
        return createLegalRestrictions(text = text).hateoas()
    }

    @GetMapping
    fun getAll(): CollectionModel<LegalRestrictionsResource> {
        return CollectionModel(findAllLegalRestrictions(), listOfNotNull(createLink()))
    }

    @GetMapping("/{id}")
    fun getOne(@PathVariable("id") id: String): ResponseEntity<EntityModel<LegalRestrictionsResource>> {
        val resource = findLegalRestrictions(id) ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        return ResponseEntity(resource.hateoas(), HttpStatus.OK)
    }

    companion object {

        fun LegalRestrictionsResource.hateoas(): EntityModel<LegalRestrictionsResource> {
            return EntityModel(
                this,
                getOneLink(this.id)
            )
        }

        fun createLink(): Link? {
            return getIfHasRole(UserRoles.UPDATE_VIDEOS) {
                linkTo(methodOn(LegalRestrictionsController::class.java).post(null)).withRel("create")
            }
        }

        fun getAllLink(): Link? {
            return getIfHasRole(UserRoles.UPDATE_VIDEOS) {
                linkTo(methodOn(LegalRestrictionsController::class.java).getAll()).withRel("legalRestrictions")
            }
        }

        fun getOneLink(id: String): Link {
            return linkTo(methodOn(LegalRestrictionsController::class.java).getOne(id)).withSelfRel()
        }
    }
}
