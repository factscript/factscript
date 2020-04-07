package io.factdriven.implementation

import io.factdriven.Flows
import io.factdriven.definition.Executing
import io.factdriven.definition.Node
import io.factdriven.definition.Promising
import io.factdriven.language.Execute
import io.factdriven.language.Sentence
import kotlin.reflect.KClass

open class ExecutingImpl<T: Any>(parent: Node):

    Execute<T>,
    Sentence<T>,
    Executing,
    ThrowingImpl<T>(parent)

{

    override val catching: KClass<*> get() = Flows.get(handling = throwing).find(nodeOfType = Promising::class)!!.succeeding!!

}
