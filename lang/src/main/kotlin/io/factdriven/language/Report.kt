package io.factdriven.language

import kotlin.reflect.KClass

@FlowLanguage
interface Report<T: Any>: ReportSuccess<T>

@FlowLanguage
interface ReportSuccess<T: Any> {

    infix fun <M: Any> success(type: KClass<M>)

}