package org.factscript.execution

import org.factscript.language.impl.utils.Json
import org.factscript.language.impl.utils.prettyJson
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

object Messages {

    private lateinit var messageStore: MessageStore
    private lateinit var messageProcessor: MessageProcessor
    private lateinit var messagePublisher: MessagePublisher

    fun register(messageStore: MessageStore) {
        Messages.messageStore = messageStore
    }

    fun register(messageProcessor: MessageProcessor) {
        Messages.messageProcessor = messageProcessor
    }

    fun register(messagePublisher: MessagePublisher) {
        Messages.messagePublisher = messagePublisher
    }

    fun load(entity: String?): List<Message> {
        return entity?.let { messageStore.load(entity) } ?: emptyList()
    }

    fun <I: Any> load(messages: List<Message>, type: KClass<I>): I {
        return messages.newInstance(type)
    }

    fun process(message: Message) {
        messageProcessor.process(message)
    }

    fun publish(vararg messages: Message) {
        messages.forEach { message -> if (message.receiver == null) message.log("PUBLISH") }
        messagePublisher.publish(*messages)
    }

    fun fromJson(json: Json): List<Message> {
        return json.asList().map { Message.fromJson(it) }
    }

    fun fromJson(json: String): List<Message> {
        return fromJson(Json(json))
    }

}