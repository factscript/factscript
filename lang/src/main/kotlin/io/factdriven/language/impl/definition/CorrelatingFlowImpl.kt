package io.factdriven.language.impl.definition

import io.factdriven.language.Await
import io.factdriven.language.Catch
import io.factdriven.language.definition.Consuming
import io.factdriven.language.definition.Correlating
import io.factdriven.language.definition.CorrelatingFlow
import io.factdriven.language.definition.Node
import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
open class CorrelatingFlowImpl<T:Any>(entity: KClass<T>, override val parent: Node? = null):

    Catch<T>,

    CorrelatingFlow,
    FlowImpl<T>(entity, parent)

{

    override val on: Await<T>
        get() {
            val child = CorrelatingImpl<T>(this)
            children.add(child)
            return child
        }

    override val consuming: KClass<*> get() = find(Consuming::class)!!.consuming
    override val correlating: Map<String, Any.() -> Any?> get() = find(Correlating::class)!!.correlating

    override val description: String get() = find(Correlating::class)!!.description

}