package io.factdriven.language.definition

import kotlin.reflect.KClass

interface Promising: Consuming {

    val successType: KClass<*>?
    val failureTypes: List<KClass<*>>

}
