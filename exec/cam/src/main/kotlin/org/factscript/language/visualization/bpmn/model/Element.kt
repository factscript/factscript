package org.factscript.language.visualization.bpmn.model

import org.factscript.language.definition.Node
import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
abstract class Element<IN: Node, OUT: Any>(val node: IN, open val parent: Element<*,*>? = null) {

    internal val process: BpmnModel get() = parent?.process ?: this as BpmnModel
    internal abstract val elements: List<Element<*,*>>

    internal abstract val model: OUT
    internal abstract val diagram: Any

    internal abstract val west: Symbol<*,*>
    internal abstract val east: Symbol<*,*>

    open fun toExecutable(): OUT {
        initModel()
        elements.forEach { it.toExecutable() }
        return model
    }

    internal abstract fun initModel()

    internal abstract fun initDiagram()

}
