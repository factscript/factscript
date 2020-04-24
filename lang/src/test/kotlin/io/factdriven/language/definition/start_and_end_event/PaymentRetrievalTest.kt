package io.factdriven.language.definition.start_and_end_event

import io.factdriven.language.Flows
import io.factdriven.language.definition.Catching
import io.factdriven.language.definition.Throwing
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class PaymentRetrievalTest {

    @Test
    fun testDefinition() {

        Flows.initialize(PaymentRetrieval::class)

        val definition = Flows.get(PaymentRetrieval::class)
        Assertions.assertEquals(PaymentRetrieval::class, definition.entity)
        Assertions.assertEquals(2, definition.children.size)

        val on = definition.find(nodeOfType = Catching::class, dealingWith = RetrievePayment::class)
        Assertions.assertEquals(PaymentRetrieval::class, on?.entity)
        Assertions.assertEquals(RetrievePayment::class, on?.catching)
        Assertions.assertEquals(definition, on?.parent)

        val emit = definition.find(nodeOfType = Throwing::class, dealingWith = PaymentRetrieved::class)
        Assertions.assertEquals(PaymentRetrieval::class, emit?.entity)
        Assertions.assertEquals(PaymentRetrieved::class, emit?.throwing)
        Assertions.assertEquals(PaymentRetrieved(3F), emit?.instance?.invoke(PaymentRetrieval(RetrievePayment(3F))))
        Assertions.assertEquals(definition, emit?.parent)

    }

}