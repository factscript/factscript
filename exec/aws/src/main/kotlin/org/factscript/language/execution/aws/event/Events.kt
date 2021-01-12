package org.factscript.language.execution.aws.event

import java.lang.RuntimeException

enum class EventReactionType {
    SUCCESS, ERROR
}

class ReactionTypeNotSupportedException(override val message : String) : RuntimeException(message){

}