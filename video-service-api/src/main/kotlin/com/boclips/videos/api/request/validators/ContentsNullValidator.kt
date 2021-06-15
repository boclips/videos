package com.boclips.videos.api.request.validators

import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import javax.validation.ReportAsSingleViolation
import kotlin.reflect.KClass

@MustBeDocumented
@Constraint(validatedBy = [NoNullContentsValidator::class])
@Target(
    AnnotationTarget.FUNCTION, AnnotationTarget.FIELD, AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.PROPERTY_GETTER
)
@Retention(AnnotationRetention.RUNTIME)
@ReportAsSingleViolation
annotation class NoNullContents(
    val message: String,
    val groups: Array<KClass<out Any>> = [],
    val payload: Array<KClass<out Any>> = []
)

class NoNullContentsValidator : ConstraintValidator<NoNullContents, Iterable<Any?>?> {
    override fun isValid(value: Iterable<Any?>?, context: ConstraintValidatorContext?): Boolean {
        return value?.none { it == null } ?: true
    }
}
