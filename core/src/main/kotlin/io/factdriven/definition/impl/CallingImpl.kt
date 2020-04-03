package io.factdriven.definition.impl

import io.factdriven.definition.Flows
import io.factdriven.definition.api.Calling
import io.factdriven.definition.api.Node
import kotlin.reflect.KClass

open class CallingImpl(parent: Node): Calling, ThrowingImpl(parent) {

    override val catching: KClass<*> get() = Flows.getPromisingNodeByCatchingType(throwing).succeeding!!

}