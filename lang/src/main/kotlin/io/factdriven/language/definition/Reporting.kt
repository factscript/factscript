package io.factdriven.language.definition

interface Reporting {

    fun isSucceeding(): Boolean
    fun isFailing(): Boolean

    fun isContinuing(): Boolean = !isSucceeding() && !isFailing()

}