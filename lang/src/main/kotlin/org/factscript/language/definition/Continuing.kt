package org.factscript.language.definition

interface Continuing {

    fun isContinuing(): Boolean = !isFinishing()
    fun isFinishing(): Boolean = isSucceeding() || isFailing() || isCompensating()
    fun isSucceeding(): Boolean
    fun isCompensating(): Boolean = false
    fun isFailing(): Boolean

}