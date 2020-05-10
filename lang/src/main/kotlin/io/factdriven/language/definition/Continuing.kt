package io.factdriven.language.definition

interface Continuing {

    fun isContinuing(): Boolean = !isFinishing()
    fun isFinishing(): Boolean = isSucceeding() || isFailing()
    fun isSucceeding(): Boolean
    fun isFailing(): Boolean

}