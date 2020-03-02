package io.factdriven.language

import io.factdriven.definition.GatewayImpl
import io.factdriven.definition.GatewayType
import io.factdriven.definition.Node
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

class SelectImpl<T: Any>(parent: Node): Select<T>, SelectOr<T>, GatewayImpl(parent) {

    override fun either(path: ConditionalExecution<T>.() -> Unit): SelectOr<T> {
        gatewayType = GatewayType.Exclusive
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