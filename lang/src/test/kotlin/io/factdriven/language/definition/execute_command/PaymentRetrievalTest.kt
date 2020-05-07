package io.factdriven.language.definition.execute_command

import io.factdriven.language.Flows
import io.factdriven.language.definition.Consuming
import io.factdriven.language.definition.Executing
import io.factdriven.language.definition.Throwing
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class PaymentRetrievalTest {

    init {
        Flows.initialize(PaymentRetrieval::class)
        Flows.initialize(CreditCardCharge::class)
    }

    @Test
    fun testDefinition() {

        val definition = Flows.get(PaymentRetrieval::class)
        Assertions.assertEquals(PaymentRetrieval::class, definition.entity)
        Assertions.assertEquals(3, definition.children.size)

        val instance = PaymentRetrieval(RetrievePayment(3F))

        val on = definition.find(nodeOfType = Consuming::class, dealingWith = RetrievePayment::class)
        Assertions.assertEquals(PaymentRetrieval::class, on?.entity)
        Assertions.assertEquals(RetrievePayment::class, on?.consuming)
        Assertions.assertEquals(definition, on?.parent)

        val execute = definition.find(nodeOfType = Executing::class, dealingWith = ChargeCreditCard::class)
        Assertions.assertEquals(PaymentRetrieval::class, execute?.entity)
        Assertions.assertEquals(ChargeCreditCard::class, execute?.throwing)
        Assertions.assertEquals(CreditCardCharged::class, execute?.succeeding)
        Assertions.assertEquals(ChargeCreditCard(instance.id, instance.total), execute?.instance?.invoke(instance))
        Assertions.assertEquals(definition, execute?.parent)

        val emit = definition.find(nodeOfType = Throwing::class, dealingWith = PaymentRetrieved::class)
        Assertions.assertEquals(PaymentRetrieval::class, emit?.entity)
        Assertions.assertEquals(PaymentRetrieved::class, emit?.throwing)
        Assertions.assertEquals(PaymentRetrieved(3F), emit?.instance?.invoke(instance))
        Assertions.assertEquals(definition, emit?.parent)

    }

}