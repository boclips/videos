package com.boclips.contentpartner.service.presentation.converters.contracts

import com.boclips.contentpartner.service.domain.model.contentpartner.Remittance
import java.util.Currency

class ContractRemittanceCurrencyConverter {
    fun fromResource(currency: String): Currency{
        return Currency.getInstance(currency)
    }
}