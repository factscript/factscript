package io.factdriven.definition.impl

import io.factdriven.definition.api.Consuming
import io.factdriven.definition.api.Executing
import io.factdriven.definition.api.Throwing
import io.factdriven.execution.Type
import io.factdriven.execution.type
import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
abstract class ExecutingImpl(override val parent: Executing?, override val entityType: KClass<*> = parent!!.entityType): Executing {

    override val children: MutableList<Executing> = mutableListOf()
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

    override val isFirstChild: Boolean get() {
        return parent == null || parent!!.children.first() == this
    }

    override val isLastChild: Boolean get() {
        return parent == null || parent!!.children.last() == this
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
