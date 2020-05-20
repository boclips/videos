package com.boclips.videos.api.request.channel

import java.util.Currency
import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import javax.validation.ReportAsSingleViolation
import kotlin.reflect.KClass

@MustBeDocumented
@Constraint(validatedBy = [CurrencyCodeValidator::class])
@Target(
    AnnotationTarget.FUNCTION, AnnotationTarget.FIELD, AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.PROPERTY_GETTER
)
@Retention(AnnotationRetention.RUNTIME)
@ReportAsSingleViolation
annotation class CurrencyCode(
    val message: String = "Invalid ISO 4271 currency code ",
    val groups: Array<KClass<out Any>> = [],
    val payload: Array<KClass<out Any>> = []
)

class CurrencyCodeValidator : ConstraintValidator<CurrencyCode, String> {
    override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
        value ?: return true
        return Currency.getAvailableCurrencies().any { it.currencyCode == value }
    }
}
