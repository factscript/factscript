package io.factdriven.language.visualization.bpmn.model

import io.factdriven.language.definition.Node
import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
abstract class Element<IN: Node, OUT: Any>(val node: IN, open val parent: Element<*,*>? = null) {

    internal abstract val children: List<Element<*,*>>

    internal abstract val diagram: Any

    internal abstract val model: OUT

    internal abstract val west: Symbol<*,*>
    internal abstract val east: Symbol<*,*>

    internal val process: BpmnModel get() = parent?.process ?: this as BpmnModel

    open fun toExecutable(): OUT {
        initModel()
        children.forEach { it.toExecutable() }
        return model
    }

    internal abstract fun initModel()

    internal abstract fun initDiagram()

    companion object {

        @Suppress("UNCHECKED_CAST")
        internal inline fun <reified T: Element<*,*>> Element<*,*>.asType(type: KClass<T> = T::class): T? = (if (type.isInstance(this)) this else null) as T?

    }

}
