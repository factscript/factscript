package io.factdriven.language.examples.payment1

import io.factdriven.definition.Flows
import io.factdriven.definition.getCatching
import io.factdriven.definition.getThrowing
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class PaymentRetrievalTest {

    @Test
    fun testDefinition() {

        val definition = Flows.getDefinitionByType(PaymentRetrieval::class)
        Assertions.assertEquals(PaymentRetrieval::class, definition.entity)
        Assertions.assertEquals(2, definition.children.size)

        val on = definition.getCatching(RetrievePayment::class)
        Assertions.assertEquals(PaymentRetrieval::class, on.entity)
        Assertions.assertEquals(RetrievePayment::class, on.catching)
        Assertions.assertEquals(definition, on.parent)

        val emit = definition.getThrowing(PaymentRetrieved::class)
        Assertions.assertEquals(PaymentRetrieval::class, emit.entity)
        Assertions.assertEquals(PaymentRetrieved::class, emit.throwing)
        Assertions.assertEquals(PaymentRetrieved(3F), emit.instance.invoke(PaymentRetrieval(RetrievePayment(3F))))
        Assertions.assertEquals(definition, emit.parent)

    }

}