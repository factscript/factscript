package io.factdriven.language.execution.aws.event.repository

import io.factdriven.execution.Message

data class EventEntity(val token: String, val messages : List<Message>, val reference: String)