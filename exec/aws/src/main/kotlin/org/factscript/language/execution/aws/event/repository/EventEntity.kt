package org.factscript.language.execution.aws.event.repository

import org.factscript.execution.Message
import org.factscript.language.execution.aws.event.EventReactionType


data class EventEntity(val token: String, val messages : List<Message>, val reference: String, val reactionType: EventReactionType, val errorCode : String?)