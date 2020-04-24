package io.factdriven.language.impl.definition

import io.factdriven.language.definition.Branching
import io.factdriven.language.definition.Node
import io.factdriven.language.*
import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
open class TriggeredExecutionImpl<T:Any>(entity: KClass<T>, override val parent: Node? = null):

    TriggeredExecution<T>,

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
            val child = CallingImpl<T>(this)
            children.add(child)
            return child
        }

    override val select: Select<T>
        get() {
            val child = BranchingImpl<T>(this)
            children.add(child)
            return child
        }

    override val loop: LoopingExecution<T>
        get() {
            @Suppress("UNCHECKED_CAST")
            val child = LoopingExecutionImpl<T>(entity as KClass<T>, this)
            children.add(child)
            return child
        }

    override val forward: Node? get() = if (parent is Branching) parent?.forward else nextSibling ?: parent?.forward
    override val backward: Node? get() = if (parent is Branching) parent?.backward else previousSibling ?: parent?.backward

}