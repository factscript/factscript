package io.factdriven.language

import io.factdriven.definition.api.Gateway.Exclusive
import io.factdriven.definition.api.Executing
import io.factdriven.definition.impl.BranchingImpl
import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
@FlowLang
interface Select<T: Any>: SelectEither<T>, Labeled<Select<T>>

@FlowLang
interface SelectEither<T: Any> {

    infix fun either(path: ConditionalExecution<T>.() -> Unit): SelectOr<T>

}

@FlowLang
interface SelectAll<T: Any> {

    infix fun all(path: ConditionalExecution<T>.() -> Unit): SelectOr<T>

}

@FlowLang
interface SelectOr<T:Any> {

    infix fun or(path: ConditionalExecution<T>.() -> Unit): SelectOr<T>

}

class SelectImpl<T: Any>(parent: Executing): Select<T>, SelectOr<T>, BranchingImpl(parent) {

    override fun either(path: ConditionalExecution<T>.() -> Unit): SelectOr<T> {
        gateway = Exclusive
        @Suppress("UNCHECKED_CAST")
        val flow = ConditionalExecutionImpl<T>(entityType as KClass<T>, this).apply(path)
        children.add(flow)
        return this
    }

    override fun or(path: ConditionalExecution<T>.() -> Unit): SelectOr<T> {
        return either(path)
    }

    override fun invoke(case: String): Select<T> {
        this.label = case
        return this
    }

}