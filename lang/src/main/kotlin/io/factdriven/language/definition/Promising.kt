package io.factdriven.language.definition

import kotlin.reflect.KClass

interface Promising: Consuming {

    val succeeding: KClass<*>?

    val failing: List<KClass<*>>

}
