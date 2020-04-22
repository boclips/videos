package com.boclips.contentpartner.service.domain.model.contentpartnercontract

sealed class ContractFilter {
    data class NameFilter(val name: String) : ContractFilter()
}
