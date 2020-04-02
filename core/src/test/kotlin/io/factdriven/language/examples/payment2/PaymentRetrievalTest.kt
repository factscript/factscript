package io.factdriven.language.examples.payment2

import io.factdriven.definition.Definition
import io.factdriven.definition.Definitions
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

        val definition = Definitions.getDefinitionByType(PaymentRetrieval::class)
        Assertions.assertEquals(PaymentRetrieval::class, definition.entityType)
        Assertions.assertEquals(4, definition.children.size)

        val instance = PaymentRetrieval(RetrievePayment(3F))

        val on = definition.getCatching(RetrievePayment::class)
        Assertions.assertEquals(PaymentRetrieval::class, on.entityType)
        Assertions.assertEquals(RetrievePayment::class, on.catching)
        Assertions.assertEquals(definition, on.parent)


        val issue = definition.getThrowing(ChargeCreditCard::class)
        Assertions.assertEquals(PaymentRetrieval::class, issue.entityType)
        Assertions.assertEquals(ChargeCreditCard::class, issue.throwing)
        Assertions.assertEquals(ChargeCreditCard(instance.id, instance.total), issue.instance.invoke(instance))
        Assertions.assertEquals(definition, issue.parent)

        val notice = definition.getCatching(CreditCardCharged::class)
        Assertions.assertEquals(PaymentRetrieval::class, notice.entityType)
        Assertions.assertEquals(CreditCardCharged::class, notice.catching)
        Assertions.assertEquals(definition, notice.parent)
        Assertions.assertEquals(1, notice.properties.size)
        Assertions.assertEquals(1, notice.matching.size)
        Assertions.assertEquals("id", notice.properties[0])
        Assertions.assertEquals(instance.id, notice.matching[0].invoke(instance))

        val emit = definition.getThrowing(PaymentRetrieved::class)
        Assertions.assertEquals(PaymentRetrieval::class, emit.entityType)
        Assertions.assertEquals(PaymentRetrieved::class, emit.throwing)
        Assertions.assertEquals(PaymentRetrieved(3F), emit.instance.invoke(instance))
        Assertions.assertEquals(definition, emit.parent)

    }

}