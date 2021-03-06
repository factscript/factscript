package org.factscript.language

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
@FlowLanguage
interface Select<T: Any>: SelectEither<T>, SelectAll<T>, Labeled<Select<T>>

@FlowLanguage
interface SelectEither<T: Any> {

    infix fun either(path: Option<T>.() -> Unit): SelectOr<T>

}

@FlowLanguage
interface SelectAll<T: Any> {

    infix fun all(path: Option<T>.() -> Unit): SelectOr<T>

}

@FlowLanguage
interface SelectOr<T:Any> {

    infix fun or(path: Option<T>.() -> Unit): SelectOr<T>

}
