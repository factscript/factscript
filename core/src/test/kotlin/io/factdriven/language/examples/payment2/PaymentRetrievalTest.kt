package io.factdriven.language.examples.payment2

import io.factdriven.Flows
import io.factdriven.definition.Catching
import io.factdriven.definition.Consuming
import io.factdriven.definition.Throwing
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class PaymentRetrievalTest {

    @Test
    fun testDefinition() {

        val definition = Flows.get(PaymentRetrieval::class)
        Assertions.assertEquals(PaymentRetrieval::class, definition.entity)
        Assertions.assertEquals(4, definition.children.size)

        val instance = PaymentRetrieval(RetrievePayment(3F))

        val on = definition.find(nodeOfType = Catching::class, dealingWith = RetrievePayment::class)
        Assertions.assertEquals(PaymentRetrieval::class, on?.entity)
        Assertions.assertEquals(RetrievePayment::class, on?.catching)
        Assertions.assertEquals(definition, on?.parent)


        val issue = definition.find(nodeOfType = Throwing::class, dealingWith = ChargeCreditCard::class)
        Assertions.assertEquals(PaymentRetrieval::class, issue?.entity)
        Assertions.assertEquals(ChargeCreditCard::class, issue?.throwing)
        Assertions.assertEquals(ChargeCreditCard(instance.id, instance.total), issue?.instance?.invoke(instance))
        Assertions.assertEquals(definition, issue?.parent)

        val notice = definition.find(nodeOfType = Consuming::class, dealingWith = CreditCardCharged::class)
        Assertions.assertEquals(PaymentRetrieval::class, notice?.entity)
        Assertions.assertEquals(CreditCardCharged::class, notice?.catching)
        Assertions.assertEquals(definition, notice?.parent)
        Assertions.assertEquals(1, notice?.properties?.size)
        Assertions.assertEquals(1, notice?.matching?.size)
        Assertions.assertEquals("id", notice?.properties?.get(0))
        Assertions.assertEquals(instance.id, notice?.matching?.get(0)?.invoke(instance))

        val emit = definition.find(nodeOfType = Throwing::class, dealingWith = PaymentRetrieved::class)
        Assertions.assertEquals(PaymentRetrieval::class, emit?.entity)
        Assertions.assertEquals(PaymentRetrieved::class, emit?.throwing)
        Assertions.assertEquals(PaymentRetrieved(3F), emit?.instance?.invoke(instance))
        Assertions.assertEquals(definition, emit?.parent)

    }

}