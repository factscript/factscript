package io.factdriven.execution

import io.factdriven.impl.execution.Message

interface MessageStore {
    fun load(id: String): List<Message>
}