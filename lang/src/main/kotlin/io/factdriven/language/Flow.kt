package io.factdriven.language

import io.factdriven.definition.Flow

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
@DslMarker annotation class FlowLang

@FlowLang
interface Flow<T:Any>: Api<T>, Execution<T>

@FlowLang
interface Api<T: Any> {

    val on: On<T>

}
