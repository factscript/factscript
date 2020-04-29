package io.factdriven.language.definition

interface Terminable {

    fun isSucceeding(): Boolean
    fun isFailing(): Boolean
    fun isContinuing(): Boolean

}