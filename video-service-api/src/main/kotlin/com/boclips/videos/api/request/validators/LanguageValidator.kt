package com.boclips.videos.api.request.validators

import java.util.Locale
import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import javax.validation.ReportAsSingleViolation
import kotlin.reflect.KClass

@MustBeDocumented
@Constraint(validatedBy = [LanguageValidator::class])
@Target(
    AnnotationTarget.FUNCTION, AnnotationTarget.FIELD, AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.PROPERTY_GETTER
)
@Retention(AnnotationRetention.RUNTIME)
@ReportAsSingleViolation
annotation class Language(
    val message: String = "Invalid ISO 639-2/T language code",
    val groups: Array<KClass<out Any>> = [],
    val payload: Array<KClass<out Any>> = []
)

object Languages {
    val validIsO3Languages: Map<String, Locale> by lazy {
        val languages = Locale.getISOLanguages()
        languages.map {
            val locale = Locale(it)
            locale.isO3Language to locale
        }.toMap()
    }
}

class LanguageValidator : ConstraintValidator<Language, String> {
    override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
        value ?: return true
        return Languages.validIsO3Languages[value.toLowerCase()] != null
    }
}
