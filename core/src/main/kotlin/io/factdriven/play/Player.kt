package io.factdriven.play

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
interface Processor {

    fun handle(message: Message)

}

interface Publisher {

    fun handle(message: Message)

}

object Player {

    private lateinit var processor: Processor
    private lateinit var publisher: Publisher

    fun register(processor: Processor) {
        this.processor = processor
    }

    fun register(publisher: Publisher) {
        this.publisher = publisher
    }

    fun process(message: Message) {
        processor.handle(message)
    }

    fun publish(message: Message) {
        publisher.handle(message)
    }

}