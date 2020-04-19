package io.factdriven.language

@FlowLang
interface Loop<T: Any>: Execution<T> {

    operator fun invoke(path: Loop<T>.() -> Unit)

    val until: Until<T>

}