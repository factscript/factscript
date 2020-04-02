package io.factdriven.language.examples.payment3

import io.factdriven.definition.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class PaymentRetrievalTest {

    init {
        Definitions.init(PaymentRetrieval::class)
        Definitions.init(CreditCardCharge::class)
    }

    @Test
    fun testDefinition() {

        val all = Definitions.all

        val definition = Definitions.getDefinitionByType(PaymentRetrieval::class)
        Assertions.assertEquals(PaymentRetrieval::class, definition.entityType)
        Assertions.assertEquals(3, definition.children.size)

        val instance = PaymentRetrieval(RetrievePayment(3F))

        val on = definition.getCatching(RetrievePayment::class)
        Assertions.assertEquals(PaymentRetrieval::class, on.entityType)
        Assertions.assertEquals(RetrievePayment::class, on.catching)
        Assertions.assertEquals(definition, on.parent)

        val execute = definition.getExecuting(ChargeCreditCard::class)
        Assertions.assertEquals(PaymentRetrieval::class, execute.entityType)
        Assertions.assertEquals(ChargeCreditCard::class, execute.throwing)
        Assertions.assertEquals(CreditCardCharged::class, execute.catching)
        Assertions.assertEquals(ChargeCreditCard(instance.id, instance.total), execute.instance.invoke(instance))
        Assertions.assertEquals(definition, execute.parent)

        val emit = definition.getThrowing(PaymentRetrieved::class)
        Assertions.assertEquals(PaymentRetrieval::class, emit.entityType)
        Assertions.assertEquals(PaymentRetrieved::class, emit.throwing)
        Assertions.assertEquals(PaymentRetrieved(3F), emit.instance.invoke(instance))
        Assertions.assertEquals(definition, emit.parent)

    }

}