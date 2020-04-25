package io.factdriven.language

import kotlin.reflect.KClass

@FlowLanguage
interface Report<T: Any>: ReportSuccess<T>, ReportFailure<T>

@FlowLanguage
interface ReportSuccess<T: Any> {

    infix fun <M: Any> success(type: KClass<M>)

}

@FlowLanguage
interface ReportFailure<T: Any> {

    infix fun <M: Any> failure(type: KClass<M>)

}