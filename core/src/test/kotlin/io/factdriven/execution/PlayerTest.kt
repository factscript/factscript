package io.factdriven.execution

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class PlayerTest {

    data class SomeFact(val property: String)

    @Test
    fun testProcessor() {

        val message = Message(Any::class, Fact(SomeFact("someValue")))
        var result: Message? = null

        val processor = object: Processor {
            override fun process(message: Message) {
                result = message
            }
        }
        Player.register(processor)
        Player.process(message)

        Assertions.assertEquals(result, message)

    }

    @Test
    fun testPublisher() {

        val message = Message(Any::class, Fact(SomeFact("someValue")))
        var result: Message? = null

        val publisher = object: Publisher {
            override fun publish(vararg message: Message) {
                result = message.first()
            }
        }
        Player.register(publisher)
        Player.publish(message)

        Assertions.assertEquals(result, message)

    }

    @Test
    fun testRepository() {

        val message = Message(Any::class, Fact(SomeFact("someValue")))

        val repository = object: Repository {
            override fun load(id: String): List<Message> {
                return listOf(message)
            }
        }
        Player.register(repository)

        Assertions.assertEquals(message, Player.load(message.id.hash).first())

    }

}