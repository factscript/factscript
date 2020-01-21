package io.factdriven.play

import io.factdriven.def.Fact
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class PlayerTest {

    data class SomeFact(val property: String)

    @Test
    fun testProcessor() {

        val message = Message(Fact(SomeFact("someValue")))
        var result: Message? = null

        val processor = object: Processor {
            override fun handle(message: Message) {
                result = message
            }
        }
        Player.register(processor)
        Player.process(message)

        Assertions.assertEquals(result, message)

    }

    @Test
    fun testPublisher() {

        val message = Message(Fact(SomeFact("someValue")))
        var result: Message? = null

        val publisher = object: Publisher {
            override fun handle(message: Message) {
                result = message
            }
        }
        Player.register(publisher)
        Player.publish(message)

        Assertions.assertEquals(result, message)

    }

}