package io.factdriven.language.definition

import kotlin.reflect.KClass

interface Succeeding {

    val succeeding: KClass<*>?

}

interface Failing {

    val failing: List<KClass<*>>

}