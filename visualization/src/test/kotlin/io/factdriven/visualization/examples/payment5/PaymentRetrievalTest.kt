package io.factdriven.visualization.examples.payment5

import io.factdriven.Flows
import io.factdriven.visualization.render
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class PaymentRetrievalTest {

    @Test
    fun testView() {

        Flows.initialize(PaymentRetrieval::class, CreditCardCharge::class)
        render(Flows.get(PaymentRetrieval::class))

    }

}