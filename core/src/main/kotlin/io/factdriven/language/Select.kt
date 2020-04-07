package io.factdriven.language

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
