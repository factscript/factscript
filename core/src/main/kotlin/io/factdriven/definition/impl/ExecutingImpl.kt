package io.factdriven.definition.impl

import io.factdriven.definition.api.Consuming
import io.factdriven.definition.api.Node
import io.factdriven.definition.api.Throwing
import io.factdriven.execution.Type
import io.factdriven.execution.type
import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
abstract class ExecutingImpl(override val parent: Node?, override val entityType: KClass<*> = parent!!.entityType): Node {

    override val children: MutableList<Node> = mutableListOf()
    override var label: String = ""; get() = if (field != "") field else type.toLabel()

    override val id: String get() {
        val id = StringBuffer(parent?.id ?: type.context)
        id.append("-")
        id.append(type.local)
        if (parent != null)
            id.append("-").append(numberOfEntityType)
        return id.toString()
    }

    override val type: Type
        get() {
            return when (this) {
                is Throwing -> throwing.type
                is Consuming -> catching.type
                else -> entityType.type
            }
        }

    override val index: Int get() {
        return parent?.children?.indexOf(this) ?: 0
    }

    private val numberOfEntityType: Int get() {
        return parent?.children?.count {
            it.type == type && it.index <= index
        } ?: 0
    }

}
