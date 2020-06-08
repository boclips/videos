package com.boclips.contentpartner.service.domain.model.contract.legalrestrictions

interface ContractLegalRestrictionsRepository {
    fun findAll(): List<ContractLegalRestriction>
    fun create(text: String): ContractLegalRestriction
}
