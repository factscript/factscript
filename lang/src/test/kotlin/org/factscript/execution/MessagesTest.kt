package org.factscript.execution

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class MessagesTest {

    data class SomeFact(val property: String)

    @Test
    fun testProcessor() {

        val message = Message(Any::class, Fact(SomeFact("someValue")))
        var result: Message? = null

        val processor = object: MessageProcessor {
            override fun process(message: Message) {
                result = message
            }
        }
        Messages.register(processor)
        Messages.process(message)

        Assertions.assertEquals(result, message)

    }

    @Test
    fun testPublisher() {

        val message = Message(Any::class, Fact(SomeFact("someValue")))
        var result: Message? = null

        val publisher = object: MessagePublisher {
            override fun publish(vararg message: Message) {
                result = message.first()
            }
        }
        Messages.register(publisher)
        Messages.publish(message)

        Assertions.assertEquals(result, message)

    }

    @Test
    fun testRepository() {

        val message = Message(Any::class, Fact(SomeFact("someValue")))

        val repository = object: MessageStore {
            override fun load(id: String): List<Message> {
                return listOf(message)
            }
        }
        Messages.register(repository)

        Assertions.assertEquals(message, Messages.load(message.id.hash).first())

    }

}