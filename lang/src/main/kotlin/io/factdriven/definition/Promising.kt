package io.factdriven.definition

import kotlin.reflect.KClass

interface Promising: Awaiting {

    val succeeding: KClass<*>?

}