package io.factdriven.impl.definition

import io.factdriven.definition.Branching
import io.factdriven.definition.Node
import io.factdriven.language.*
import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
open class FlowImpl<T:Any>(entity: KClass<T>, override val parent: Node? = null):

    Flow<T>,

    NodeImpl(parent, entity)

{

    override val on: On<T>
        get() {
            val child = PromisingImpl<T>(this)
            children.add(child)
            return child
        }

    override val emit: Emit<T>
        get() {
            val child = ThrowingImpl<T>(this)
            children.add(child)
            return child
        }

    override val issue: Issue<T>
        get() {
            val child = ThrowingImpl<T>(this)
            children.add(child)
            return child
        }

    override val await: Await<T>
        get() {
            val child = AwaitingImpl<T>(this)
            children.add(child)
            return child
        }

    override val execute: Execute<T>
        get() {
            val child = ExecutingImpl<T>(this)
            children.add(child)
            return child
        }

    override val select: Select<T>
        get() {
            val child = BranchingImpl<T>(this)
            children.add(child)
            return child
        }

    override val forward: Node? get() = if (parent is Branching) parent?.forward else nextSibling ?: parent?.forward
    override val backward: Node? get() = if (parent is Branching) parent?.backward else previousSibling ?: parent?.backward

}