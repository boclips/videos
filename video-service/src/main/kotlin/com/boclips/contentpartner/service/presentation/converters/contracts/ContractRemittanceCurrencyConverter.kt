package com.boclips.contentpartner.service.presentation.converters.contracts

import java.util.Currency

class ContractRemittanceCurrencyConverter {
    fun fromResource(currency: String): Currency {
        return Currency.getInstance(currency)
    }
}
