package io.factdriven.language.execution.aws.event.repository

import io.factdriven.execution.Message
import io.factdriven.language.execution.aws.event.EventReactionType


data class EventEntity(val token: String, val messages : List<Message>, val reference: String, val reactionType: EventReactionType, val errorCode : String?)