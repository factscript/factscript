package io.factdriven.execution

import io.factdriven.impl.execution.Message

interface MessageProcessor {
    fun process(message: Message)
}