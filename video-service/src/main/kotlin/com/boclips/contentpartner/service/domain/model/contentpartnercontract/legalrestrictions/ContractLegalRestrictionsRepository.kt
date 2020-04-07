package com.boclips.contentpartner.service.domain.model.contentpartnercontract.legalrestrictions

interface ContractLegalRestrictionsRepository {
    fun findAll(): List<ContractLegalRestriction>
    fun create(text: String): ContractLegalRestriction
}