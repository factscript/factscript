package io.factdriven.definition.api

import io.factdriven.definition.api.Consuming
import kotlin.reflect.KClass

interface Promising: Consuming {

    val succeeding: KClass<*>?

}