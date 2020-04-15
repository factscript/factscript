package io.factdriven.definition

import io.factdriven.definition.Consuming
import kotlin.reflect.KClass

interface Promising: Consuming {

    val succeeding: KClass<*>?

}