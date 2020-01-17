package io.factdriven.language.examples.payment1

import io.factdriven.language.Flow
import io.factdriven.language.examples.creditcard1.CreditCardCharge
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class PaymentRetrievalTest {

    @Test
    fun testDefinition() {

        val definition = Flow.get(PaymentRetrieval::class)
        Assertions.assertEquals(PaymentRetrieval::class, definition.entityType)
        Assertions.assertEquals(2, definition.children.size)

        val on = definition.getCatching(RetrievePayment::class)
        Assertions.assertEquals(PaymentRetrieval::class, on.entityType)
        Assertions.assertEquals(RetrievePayment::class, on.catchingType)
        Assertions.assertEquals(definition, on.parent)

        val emit = definition.getThrowing(PaymentRetrieved::class)
        Assertions.assertEquals(PaymentRetrieval::class, emit.entityType)
        Assertions.assertEquals(PaymentRetrieved::class, emit.throwingType)
        Assertions.assertEquals(PaymentRetrieved(3F), emit.constructor.invoke(PaymentRetrieval(RetrievePayment(3F))))
        Assertions.assertEquals(definition, emit.parent)

    }

}