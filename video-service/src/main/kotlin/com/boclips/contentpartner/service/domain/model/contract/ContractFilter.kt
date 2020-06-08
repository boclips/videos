package com.boclips.contentpartner.service.domain.model.contract

sealed class ContractFilter {
    data class NameFilter(val name: String) : ContractFilter()
}
