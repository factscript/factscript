package io.factdriven.visualization.examples.payment3

import io.factdriven.Flows
import io.factdriven.visualization.render
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class PaymentRetrievalTest {

    @Test
    fun testView() {
        render(Flows.init(CreditCardCharge::class, PaymentRetrieval::class)[1])
    }

}