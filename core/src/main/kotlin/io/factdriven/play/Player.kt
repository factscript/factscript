package io.factdriven.play

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
interface Processor {
    fun process(message: Message)
}

interface Publisher {
    fun publish(vararg message: Message)
}

interface Repository {
    fun load(id: String): List<Message>
}

object Player {

    private lateinit var repository: Repository
    private lateinit var processor: Processor
    private lateinit var publisher: Publisher

    val log: Logger = LoggerFactory.getLogger(Player::class.java)

    fun register(repository: Repository) {
        this.repository = repository
    }

    fun register(processor: Processor) {
        this.processor = processor
    }

    fun register(publisher: Publisher) {
        this.publisher = publisher
    }

    fun load(id: String?): List<Message> {
        return id?.let { repository.load(id) } ?: emptyList()
    }

    fun <I: Any> load(id: String, type: KClass<I>): I {
        return load(load(id), type)
    }

    fun <I: Any> load(messages: List<Message>, type: KClass<I>): I {
        val instance = messages.applyTo(type)
        log.debug("Loading aggregate ${type.name} [${messages.first().id}]\n${instance.toJson()}")
        return instance
    }

    fun process(message: Message) {
        log.debug("Processing message ${message.fact.name} [${message.id}]\n${message.toJson()}")
        processor.process(message)
    }

    fun publish(vararg messages: Message) {
        messages.forEach { message ->
            log.debug("Publishing message ${message.fact.name} [${message.id}]\n${message.toJson()}")
        }
        publisher.publish(*messages)
    }

}