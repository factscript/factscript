package io.factdriven.language.examples.payment1

import io.factdriven.language.Flow
import io.factdriven.language.examples.creditcard1.CreditCardCharge
import io.factdriven.view.render
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class PaymentRetrievalTest {

    @Test
    fun testDefinition() {

        render(Flow.get(PaymentRetrieval::class))

    }

}