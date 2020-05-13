package io.factdriven.language.definition.start_and_end_event

import io.factdriven.language.*
import io.factdriven.language.definition.Consuming
import io.factdriven.language.definition.Throwing
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class PaymentRetrievalTest {

    @Test
    fun testDefinition() {

        Flows.activate(PaymentRetrieval::class)

        val definition = Flows.get(PaymentRetrieval::class)
        Assertions.assertEquals(PaymentRetrieval::class, definition.entity)
        Assertions.assertEquals(2, definition.children.size)

        val on = definition.find(nodeOfType = Consuming::class, dealingWith = RetrievePayment::class)
        Assertions.assertEquals(PaymentRetrieval::class, on?.entity)
        Assertions.assertEquals(RetrievePayment::class, on?.consuming)
        Assertions.assertEquals(definition, on?.parent)

        val emit = definition.find(nodeOfType = Throwing::class, dealingWith = PaymentRetrieved::class)
        Assertions.assertEquals(PaymentRetrieval::class, emit?.entity)
        Assertions.assertEquals(PaymentRetrieved::class, emit?.throwing)
        Assertions.assertEquals(PaymentRetrieved(3F), emit?.factory?.invoke(PaymentRetrieval(RetrievePayment(3F))))
        Assertions.assertEquals(definition, emit?.parent)

    }

}