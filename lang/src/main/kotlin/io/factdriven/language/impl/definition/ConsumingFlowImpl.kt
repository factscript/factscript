package io.factdriven.language.impl.definition

import io.factdriven.language.*
import io.factdriven.language.definition.*
import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
open class ConsumingFlowImpl<T:Any>(entity: KClass<T>, override val parent: Node? = null):

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
    override val matching: List<Any.() -> Any?> get() = find(Correlating::class)!!.matching
    override val properties: List<String> get() = find(Correlating::class)!!.properties

}