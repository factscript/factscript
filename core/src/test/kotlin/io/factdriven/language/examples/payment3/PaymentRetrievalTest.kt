package io.factdriven.language.examples.payment3

import io.factdriven.definition.Definition
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class PaymentRetrievalTest {

    init {
        Definition.init(PaymentRetrieval::class)
        Definition.init(CreditCardCharge::class)
    }

    @Test
    fun testDefinition() {

        val all = Definition.all

        val definition = Definition.getDefinitionByType(PaymentRetrieval::class)
        Assertions.assertEquals(PaymentRetrieval::class, definition.entityType)
        Assertions.assertEquals(3, definition.children.size)

        val instance = PaymentRetrieval(RetrievePayment(3F))

        val on = definition.getCatching(RetrievePayment::class)
        Assertions.assertEquals(PaymentRetrieval::class, on.entityType)
        Assertions.assertEquals(RetrievePayment::class, on.catchingType)
        Assertions.assertEquals(definition, on.parent)

        val execute = definition.getExecuting(ChargeCreditCard::class)
        Assertions.assertEquals(PaymentRetrieval::class, execute.entityType)
        Assertions.assertEquals(ChargeCreditCard::class, execute.throwingType)
        Assertions.assertEquals(CreditCardCharged::class, execute.catchingType)
        Assertions.assertEquals(ChargeCreditCard(instance.id, instance.total), execute.constructor.invoke(instance))
        Assertions.assertEquals(definition, execute.parent)

        val emit = definition.getThrowing(PaymentRetrieved::class)
        Assertions.assertEquals(PaymentRetrieval::class, emit.entityType)
        Assertions.assertEquals(PaymentRetrieved::class, emit.throwingType)
        Assertions.assertEquals(PaymentRetrieved(3F), emit.constructor.invoke(instance))
        Assertions.assertEquals(definition, emit.parent)

    }

}