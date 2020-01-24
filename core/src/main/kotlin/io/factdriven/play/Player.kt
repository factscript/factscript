package io.factdriven.play

import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
interface Processor {

    fun handle(message: Message)

}

interface Publisher {

    fun handle(message: Message)

}

interface Repository {

    fun load(id: String): List<Message>

}

object Player {

    private lateinit var repository: Repository
    private lateinit var processor: Processor
    private lateinit var publisher: Publisher

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
        return load(id).applyTo(type)
    }

    fun process(message: Message) {
        processor.handle(message)
    }

    fun publish(message: Message) {
        publisher.handle(message)
    }

}