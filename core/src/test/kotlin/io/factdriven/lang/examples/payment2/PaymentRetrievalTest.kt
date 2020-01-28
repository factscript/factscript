package io.factdriven.lang.examples.payment2

import io.factdriven.def.Definition
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class PaymentRetrievalTest {

    @Test
    fun testDefinition() {

        val definition = Definition.getDefinitionByType(PaymentRetrieval::class)
        Assertions.assertEquals(PaymentRetrieval::class, definition.entityType)
        Assertions.assertEquals(4, definition.children.size)

        val instance = PaymentRetrieval(RetrievePayment(3F))

        val on = definition.getCatching(RetrievePayment::class)
        Assertions.assertEquals(PaymentRetrieval::class, on.entityType)
        Assertions.assertEquals(RetrievePayment::class, on.catchingType)
        Assertions.assertEquals(definition, on.parent)


        val issue = definition.getThrowing(ChargeCreditCard::class)
        Assertions.assertEquals(PaymentRetrieval::class, issue.entityType)
        Assertions.assertEquals(ChargeCreditCard::class, issue.throwingType)
        Assertions.assertEquals(ChargeCreditCard(instance.id, instance.total), issue.constructor.invoke(instance))
        Assertions.assertEquals(definition, issue.parent)

        val notice = definition.getCatching(CreditCardCharged::class)
        Assertions.assertEquals(PaymentRetrieval::class, notice.entityType)
        Assertions.assertEquals(CreditCardCharged::class, notice.catchingType)
        Assertions.assertEquals(definition, notice.parent)
        Assertions.assertEquals(1, notice.catchingProperties.size)
        Assertions.assertEquals(1, notice.matchingValues.size)
        Assertions.assertEquals("id", notice.catchingProperties[0])
        Assertions.assertEquals(instance.id, notice.matchingValues[0].invoke(instance))

        val emit = definition.getThrowing(PaymentRetrieved::class)
        Assertions.assertEquals(PaymentRetrieval::class, emit.entityType)
        Assertions.assertEquals(PaymentRetrieved::class, emit.throwingType)
        Assertions.assertEquals(PaymentRetrieved(3F), emit.constructor.invoke(instance))
        Assertions.assertEquals(definition, emit.parent)

    }

}