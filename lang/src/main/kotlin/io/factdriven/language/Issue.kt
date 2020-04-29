package io.factdriven.language

import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
@FlowLanguage
interface Issue<T: Any>: IssueCommand<T>

@FlowLanguage
interface IssueCommand<T: Any> {

    infix fun <M: Any> command(type: KClass<M>): By<T, M>

}
