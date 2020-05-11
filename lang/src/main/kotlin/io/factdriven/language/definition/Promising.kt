package io.factdriven.language.definition

import kotlin.reflect.KClass

interface Promising: Consuming {

    val success: KClass<*>?
    val failure: List<KClass<*>>

}
