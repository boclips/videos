package com.boclips.videos.api.common

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.BeanProperty
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.deser.ContextualDeserializer
import com.fasterxml.jackson.databind.deser.std.StdDeserializer

@JsonDeserialize(using = SpecifiableDeserializer::class)
sealed class Specifiable<T> {
    fun <O> map(f: (T) -> O): Specifiable<O> =
        when (this) {
            is Specified -> Specified(value = f(value))
            is ExplicitlyNull -> ExplicitlyNull()
        }

    abstract fun orNull(): T?
}

data class Specified<T>(val value: T) : Specifiable<T>() {
    override fun orNull(): T? {
        return value
    }
}

class ExplicitlyNull<T> : Specifiable<T>() {
    override fun orNull(): T? {
        return null
    }
}

class SpecifiableDeserializer constructor(
    vc: Class<*>? = null
) : StdDeserializer<Specifiable<*>>(vc), ContextualDeserializer {

    // there's some good context on this here: https://stackoverflow.com/a/36223769

    private var containedValueType: JavaType? = null

    override fun deserialize(parser: JsonParser, context: DeserializationContext?): Specifiable<*> {
        val value: Any = context!!.readValue(parser, containedValueType)
        return Specified(value)
    }

    override fun getNullValue(ctxt: DeserializationContext?): Specifiable<*> {
        return ExplicitlyNull<Any>()
    }

    override fun createContextual(ctxt: DeserializationContext?, property: BeanProperty?): JsonDeserializer<*> {
        containedValueType = ctxt!!.contextualType.bindings.typeParameters[0]
        return this
    }
}
