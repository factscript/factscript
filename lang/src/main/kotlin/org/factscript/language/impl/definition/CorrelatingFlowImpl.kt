package org.factscript.language.impl.definition

import org.factscript.language.*
import org.factscript.language.definition.Consuming
import org.factscript.language.definition.Correlating
import org.factscript.language.definition.CorrelatingFlow
import org.factscript.language.definition.Node
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

    override fun isCompensating(): Boolean {
        return if (find(Consuming::class) != null) promise.failureTypes.contains(consuming) else false
    }

    override fun isSucceeding(): Boolean {
        return !isCompensating() && super.isSucceeding()
    }

}