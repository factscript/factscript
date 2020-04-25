package io.factdriven.language.definition

import kotlin.reflect.KClass

interface Promising: Awaiting {

    val succeeding: KClass<*>?
    val failing: List<KClass<*>>

}