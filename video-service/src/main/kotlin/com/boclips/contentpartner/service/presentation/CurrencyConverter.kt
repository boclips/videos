package com.boclips.contentpartner.service.presentation

import com.boclips.contentpartner.service.application.exceptions.InvalidCurrencyException
import java.util.Currency

object CurrencyConverter {
    fun convert(s: String): Currency {
        try {
            return Currency.getInstance(s)
        } catch (e: IllegalArgumentException) {
            throw InvalidCurrencyException(s)
        }
    }
}
