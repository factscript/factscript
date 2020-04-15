package io.factdriven.language

import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
@FlowLang
interface Issue<T: Any>: IssueCommand<T>

@FlowLang
interface IssueCommand<T: Any> {

    infix fun <M: Any> command(type: KClass<M>): Sentence<T>

}
