package org.factscript.language.execution.aws.event.repository

import org.factscript.execution.Message
import org.factscript.language.execution.aws.event.EventReactionType
import java.time.LocalDateTime

data class TimerEventEntity(val token: String, val messages : List<Message>, val dateTime: LocalDateTime, val reactionType: EventReactionType, val errorCode : String?)