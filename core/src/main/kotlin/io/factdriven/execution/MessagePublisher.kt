package io.factdriven.execution

import io.factdriven.impl.execution.Message

interface MessagePublisher {
    fun publish(vararg message: Message)
}