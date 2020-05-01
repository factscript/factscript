package io.factdriven.language.definition.issue_command

import io.factdriven.language.Flows
import io.factdriven.language.definition.Consuming
import io.factdriven.language.definition.ConsumingEvent
import io.factdriven.language.definition.Throwing
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

        val on = definition.find(nodeOfType = Consuming::class, dealingWith = RetrievePayment::class)
        Assertions.assertEquals(PaymentRetrieval::class, on?.entity)
        Assertions.assertEquals(RetrievePayment::class, on?.consuming)
        Assertions.assertEquals(definition, on?.parent)


        val issue = definition.find(nodeOfType = Throwing::class, dealingWith = ChargeCreditCard::class)
        Assertions.assertEquals(PaymentRetrieval::class, issue?.entity)
        Assertions.assertEquals(ChargeCreditCard::class, issue?.throwing)
        Assertions.assertEquals(ChargeCreditCard(instance.id, instance.total), issue?.instance?.invoke(instance))
        Assertions.assertEquals(definition, issue?.parent)

        val notice = definition.find(nodeOfType = ConsumingEvent::class, dealingWith = CreditCardCharged::class)
        Assertions.assertEquals(PaymentRetrieval::class, notice?.entity)
        Assertions.assertEquals(CreditCardCharged::class, notice?.consuming)
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